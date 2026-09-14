# 04 — Retrieval-augmented generation (RAG)

Answering from **your documents**: a PDF is ingested into a Qdrant vector store, and each question is
answered from the chunks retrieved for it. RAG is built here in three flavours, from the most explicit to
the most automated, each across the three stacks (Spring AI on Spring Boot, LangChain4j on Spring Boot,
LangChain4j on Quarkus):

- **classic** — imperative retrieval: you embed the query, call `similaritySearch`/`search` yourself, and
  stuff the hits into the prompt. See [`README-classic.md`](README-classic.md).
- **advisor** — declarative retrieval: you declare a retriever once and the framework weaves retrieval into
  every call (Spring AI `QuestionAnswerAdvisor`, LangChain4j AI Services with a `ContentRetriever` /
  `RetrievalAugmentor`). See [`README-advisor.md`](README-advisor.md).
- **modular** — multi-source retrieval + reciprocal-rank fusion: LangChain4j's `DefaultQueryRouter` +
  `DefaultContentAggregator` versus a hand-rolled multi-source `DocumentRetriever` on Spring AI. See
  [`README-modular.md`](README-modular.md).

## Structure

A single aggregate with the shared contracts nested alongside the nine stack modules:

```
04-rag/
  ├── 04-rag-common/          # contract, corpus PDF and conformance test for classic + advisor
  ├── 04-rag-modular-common/  # modular's own two-source corpus, contract and conformance test
  ├── 04-rag-classic-{spring-ai-boot, langchain4j-boot, langchain4j-quarkus}/
  ├── 04-rag-advisor-{spring-ai-boot, langchain4j-boot, langchain4j-quarkus}/
  └── 04-rag-modular-{spring-ai-boot, langchain4j-boot, langchain4j-quarkus}/
```

The classic and advisor flavours store into separate Qdrant collections (`rag-classic` / `rag-advisor`) and
share `04-rag-common`; the modular flavour uses its own `04-rag-modular-common`. Ingestion is a shared
Camel + Tika skeleton with a stack-native split/embed/store step. Each flavour ships a
`docker-compose-<flavour>.yml` (Ollama + Qdrant) for running a stack by hand.

`mvn verify` runs every stack's conformance suite against one shared Ollama container (answering +
embedding models) and one shared Qdrant container per module.
