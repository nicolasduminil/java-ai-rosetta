# 04 — RAG classic: talking with your documents

Use cases 01–03 answered from the model's own knowledge or from a short reference string. This one
answers from **your documents**: a PDF is ingested into a vector store, and each question is answered
from the chunks retrieved for it. This is **retrieval-augmented generation (RAG)**, done the
transparent way — *classic*, imperative retrieval where every step is visible in code.

RAG splits naturally into two independent parts, delivered as two sibling use cases under this chapter:

| Part | Use case | Retrieval |
|---|---|---|
| **classic** (this one) | `04-rag-classic` | imperative — you embed the query, call `similaritySearch`/`search` yourself, and stuff the hits into the prompt |
| **advisor** | `04-rag-advisor` | declarative — the framework's retrieval advisor / augmentor does it under the hood |

Both use cases share one module, **`04-rag-common`** (the `Question`/`Answer` contract, the corpus PDF,
and the conformance test), so the two differ only in their stack modules.

## Where it came from

Adapted from **chapter 4, "Talking with your documents", of *Spring AI in Action* (Craig Walls)**.
As with 01–03 the book's domain is dropped and only the mechanism is kept. Provenance here is
**by-description**, not by-source: no `_original/` was carried over, and the book targets an earlier
Spring AI milestone, so every API was verified against the pinned versions (Spring AI 2.0.0,
LangChain4j 1.17.2, Quarkus LangChain4j 1.12.2) rather than reproduced from the chapter.

## The pipeline: Camel skeleton, stack-native terminal

The book builds ingestion from Spring Function Catalog / Spring Cloud Function suppliers. We replace
that with **Apache Camel**, which is what this repository reaches for when a pipeline is involved — but
only for the part Camel is best at. The division of labour is the point:

- **Camel owns the pipeline skeleton** — a one-shot route that runs `tika:parse` to extract the PDF to
  **plain text**. This route is byte-for-byte identical across all three stacks.
- **Each stack owns the terminal** — splitting the extracted text, embedding it, and storing it, each in
  its own idiom. That is exactly the part the "Rosetta" comparison exists to show, so it stays native.

Because Tika runs inside Camel, **no stack pulls its own Tika library** — there is one Tika, in the
pipeline. (`camel-tika` ships only the HTML and text parser modules, so each module adds
`tika-parser-pdf-module` to teach Tika to read the PDF.)

The shared route, on every stack:

```java
from("direct:ingest")
  .routeId("corpus-ingest")
  .to("tika:parse?tikaParseOutputFormat=text")   // PDF bytes -> extracted text
  .bean(loader, "load");                          // stack-native split + embed + store
```

Ingestion is triggered once at startup and runs **synchronously** (Spring `ApplicationReadyEvent` /
Quarkus `StartupEvent`), so the corpus is stored before the first request — and before the conformance
test queries. A `file:` consumer would be the production-facing variant; `direct:` keeps the tests
deterministic.

## The headline asymmetry: who creates the collection?

The stacks store into the same **Qdrant** container (gRPC, port 6334), but they differ on one telling
point:

| | Spring AI | LangChain4j (Boot & Quarkus) |
|---|---|---|
| collection creation | **automatic** — the Qdrant starter creates it from the `EmbeddingModel`'s dimension when `initialize-schema=true` | **manual** — `QdrantEmbeddingStore` never creates it, so the app pre-creates the collection (768 dims, Cosine) via `QdrantClient` |
| embedding + store | `VectorStore.add(chunks)` embeds and stores in one call | `EmbeddingStoreIngestor` splits, embeds and stores |
| splitter | `TokenTextSplitter` | `DocumentSplitters.recursive(...)` |

Same capability, different amount of plumbing you write yourself — the recurring lesson of this
repository.

## Classic retrieval, side by side

Spring AI — search returns `Document`s you stuff into the prompt:

```java
var hits = vectorStore.similaritySearch(SearchRequest.builder().query(q).topK(4).build());
var context = hits.stream().map(Document::getText).collect(joining("\n\n"));
chatClient.prompt().system(s -> s.text(template).param("context", context)).user(q).call().content();
```

LangChain4j — you embed the query yourself, then search:

```java
var queryEmbedding = embeddingModel.embed(q).content();
var matches = embeddingStore.search(EmbeddingSearchRequest.builder()
    .queryEmbedding(queryEmbedding).maxResults(4).build()).matches();
var context = matches.stream().map(m -> m.embedded().text()).collect(joining("\n\n"));
chatModel.chat(SystemMessage.from(prompt.formatted(context)), UserMessage.from(q));
```

## Structure

```
04-rag-classic/
  ├── 04-rag-classic-spring-ai-boot/      # Spring AI: auto-created collection, VectorStore.add / similaritySearch
  ├── 04-rag-classic-langchain4j-boot/    # LangChain4j on Boot: pre-created collection, EmbeddingStoreIngestor
  ├── 04-rag-classic-langchain4j-quarkus/ # LangChain4j on Quarkus: same, with CDI + camel-quarkus-tika
  └── docker-compose.yml                  # Ollama + Qdrant, to run a stack by hand
```

The contract, the corpus PDF and the conformance test live in the shared `04-rag-common` module. The
corpus is a single generated PDF (`04-rag-common/src/main/resources/corpus/`) describing a
fictional company, Aurora Labs. `AbstractRagQuestionAnsweringIT` asks for facts stated **only** in that
PDF (its HQ city, its product name) and asserts the answer contains them — proof that retrieval fed the
generation — plus input-validation probes. As in 03 the assertion targets the *capability* (a grounded
answer comes back), which is reliable here because the retrieved chunk lands in the prompt exactly as an
injected reference did in 02.

## The models

Answering runs on the small `qwen2.5:0.5b`; embedding needs a dedicated model, `nomic-embed-text`
(768 dims), pulled lazily so only RAG use cases pay for it. Both share one Ollama container per module.

## Status

| Track | State |
|---|---|
| `04-rag-common` (shared) | done — contract, corpus PDF, conformance suite (test-jar) |
| `04-rag-classic-spring-ai-boot` | done — auto-created collection, imperative `similaritySearch`, passes the suite |
| `04-rag-classic-langchain4j-boot` | done — pre-created collection, imperative `search`, passes the suite |
| `04-rag-classic-langchain4j-quarkus` | done — same on Quarkus (CDI, camel-quarkus-tika), passes the suite |

`mvn verify` runs each stack's RAG flow against one shared Ollama container (answering + embedding
models) and one shared Qdrant container per module.
