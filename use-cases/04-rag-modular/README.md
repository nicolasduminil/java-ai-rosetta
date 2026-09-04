# 04 — RAG modular: a composable retrieval pipeline

The classic use case retrieved imperatively; the advisor use case handed a single retriever to the
framework. This one goes one step further: retrieval becomes a **pipeline of stages**, and the stage that
matters here is **multi-source retrieval with rank fusion** — the query is answered from *two* sources and
their hits are fused, rather than from one top-k search over a single store.

| Part | Use case | Retrieval |
|---|---|---|
| **classic** | `04-rag-classic` | imperative — you embed the query and call the store yourself |
| **advisor** | `04-rag-advisor` | declarative — one framework retriever, woven into the call |
| **modular** (this one) | `04-rag-modular` | a pipeline — route the query to one retriever *per source*, then fuse the results |

## The corpus and the probe

Two PDFs about the fictional **Aurora Labs**, ingested into one Qdrant collection (`rag-modular`) and tagged
with a `source` metadata as they are stored:

- `aurora-briefing.pdf` (`source = briefing`) — the company briefing: HQ, product Lumen, telemetry, teams.
- `aurora-press.pdf` (`source = press`) — one engineering note stating a fact found **nowhere else**: the
  telemetry analytics engine is internally codenamed **Borealis**.

The conformance probe asks for that codename. The pipeline routes the question to a **briefing** retriever
*and* a **press** retriever, so the press source's chunk is always in the retrieved context regardless of
how the two sources rank against each other in one global search. Each stack is held to the same
`AbstractModularRagQuestionAnsweringIT`: the `Borealis` fact (multi-source), a briefing fact
(`Reykjavik`), and the two input-validation probes.

> **On what the test proves.** Like every use case here, the contract proves each stack *correctly performs
> the capability* — multi-source retrieval that grounds on the minority source — not that it beats a single
> retriever on this tiny corpus (with so few chunks, a single top-k search would also find both facts). The
> value of multi-source routing is a **scaling** property: as a dominant source grows, a bounded top-k
> starves the minority source, while per-source retrieval guarantees every source contributes. The
> interesting, corpus-independent result is the **API asymmetry** below.

## The headline: the same pipeline, one framework gives it to you, the other you build

| | Spring AI | LangChain4j (Boot & Quarkus) |
|---|---|---|
| pipeline | `RetrievalAugmentationAdvisor` (module `spring-ai-rag`, not transitive) | `DefaultRetrievalAugmentor` (in `langchain4j-core`, no new dependency) |
| multi-source retrieval | **not built in** — the advisor takes one `DocumentRetriever`; we supply a `MultiSourceDocumentRetriever` that fans out over two `VectorStoreDocumentRetriever`s | **first-class** — `new DefaultQueryRouter(briefing, press)` routes the query to both retrievers |
| fusion | concatenate + de-duplicate inside the custom retriever | **first-class** — `new DefaultContentAggregator()` does reciprocal-rank fusion, no model |
| context injection | `ContextualQueryAugmenter` (the advisor's default) | `DefaultContentInjector` (the augmentor's default) |

**The finding:** LangChain4j ships routing and rank fusion as composable pipeline parts you assemble;
Spring AI's advisor centers on a *single* retriever, so the multi-source fan-out has to be hand-written.
Neither uses an LLM or a re-ranking model for this — the fusion is deterministic, which is why the test is
reproducible on the small local `qwen2.5:0.5b`.

## Multi-source retrieval, side by side

LangChain4j (Boot & Quarkus) — route to both, fuse by reciprocal rank:

```java
RetrievalAugmentor augmentor = DefaultRetrievalAugmentor.builder()
    .queryRouter(new DefaultQueryRouter(
        sourceRetriever("briefing"),      // EmbeddingStoreContentRetriever + metadata filter
        sourceRetriever("press")))
    .contentAggregator(new DefaultContentAggregator())   // RRF, no model
    .build();
```

Spring AI — one retriever slot, so fan-out is a custom `DocumentRetriever`:

```java
var retriever = new MultiSourceDocumentRetriever(   // our class: calls each delegate, concatenates, de-dups
    sourceRetriever(vectorStore, "briefing"),        // VectorStoreDocumentRetriever + filterExpression
    sourceRetriever(vectorStore, "press"));
chatClient = chatClientBuilder
    .defaultAdvisors(RetrievalAugmentationAdvisor.builder().documentRetriever(retriever).build())
    .build();
```

In every stack the RAG service stays a one-line delegate: no `similaritySearch`, no manual prompt.

## Ingestion is the advisor's, plus a source tag

Identical to `04-rag-advisor` — a one-shot Camel route runs `tika:parse` on each PDF, then a stack-native
loader splits, embeds and stores it — with one addition: the ingestion trigger sends each document with a
`source` Camel header, and the loader stamps that `source` onto every chunk's metadata
(`Metadata.from("source", …)` on LangChain4j, `new Document(text, Map.of("source", …))` on Spring AI). The
per-source retrievers then filter on it.

## Where it came from

The modular / advanced-RAG topic of **chapter 4 of *Spring AI in Action* (Craig Walls)**. Provenance is
**by-description**, not by-source: every API was verified against the pinned versions (Spring AI 2.0.0,
LangChain4j 1.17.2, Quarkus LangChain4j 1.12.2) rather than reproduced from the chapter. This use case
deliberately uses the **deterministic** stages of each pipeline (multi-source routing + rank fusion). The
LLM-driven stages — query expansion/rewrite (`MultiQueryExpander`, `ExpandingQueryTransformer`), LLM
routing, model-based re-ranking — are the production upgrade path; they are omitted here because they are
unreliable on a 0.5B local model and Ollama ships no re-ranking model, which would make the contract flaky.

## Structure

```
04-rag-modular/
  ├── 04-rag-modular-common/            # contract, two-source corpus, AbstractModularRagQuestionAnsweringIT
  ├── 04-rag-modular-spring-ai-boot/     # Spring AI: RetrievalAugmentationAdvisor + MultiSourceDocumentRetriever
  ├── 04-rag-modular-langchain4j-boot/   # LangChain4j on Boot: DefaultRetrievalAugmentor (router + RRF)
  ├── 04-rag-modular-langchain4j-quarkus/# LangChain4j on Quarkus: produced RetrievalAugmentor (router + RRF)
  └── docker-compose.yml                 # Ollama + Qdrant, to run a stack by hand
```

Unlike classic/advisor (which share `04-rag-common`), modular has its **own** `04-rag-modular-common`: its
corpus and probe differ, and keeping it separate leaves the classic and advisor tests untouched.

## The models

Answering runs on `qwen2.5:0.5b`; embedding on `nomic-embed-text` (768 dims), pulled lazily, one shared
Ollama container per module. No re-ranking model — the fusion stage is pure reciprocal-rank arithmetic.

## Status

| Track | State |
|---|---|
| `04-rag-modular-spring-ai-boot` | done — `RetrievalAugmentationAdvisor` + `MultiSourceDocumentRetriever`, passes the suite |
| `04-rag-modular-langchain4j-boot` | done — `DefaultRetrievalAugmentor` with `DefaultQueryRouter` + `DefaultContentAggregator`, passes the suite |
| `04-rag-modular-langchain4j-quarkus` | done — produced `RetrievalAugmentor` (same pipeline), passes the suite |

`mvn verify` runs each stack's modular flow against one shared Ollama container (answering + embedding
models) and one Qdrant container per module.
