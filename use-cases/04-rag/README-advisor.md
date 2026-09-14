# 04 — RAG advisor: declarative retrieval

The classic use case answered from your documents the transparent way: you embedded the query, called
the vector store yourself, and stuffed the hits into the prompt. This one answers the **same** questions
from the **same** corpus, but hands retrieval to the framework. You **declare** a retriever once and the
framework weaves the similarity search and context injection into every chat call. The service body
carries no retrieval code at all.

| Part | Use case | Retrieval |
|---|---|---|
| **classic** | `04-rag-classic` | imperative — you embed the query, call `similaritySearch`/`search` yourself, and stuff the hits into the prompt |
| **advisor** (this one) | `04-rag-advisor` | declarative — the framework's advisor / AI-service retriever does it under the hood |

Both use cases share one module, **`04-rag-common`** (the `Question`/`Answer` contract, the corpus PDF,
and the conformance test), so the two differ only in their stack modules. The advisor stacks store into a
separate Qdrant collection (`rag-advisor`), so the two use cases run independently.

## Where it came from

Adapted from **chapter 4, "Talking with your documents", of *Spring AI in Action* (Craig Walls)**, whose
RAG example is built on Spring AI's `QuestionAnswerAdvisor`. Provenance is **by-description**, not
by-source. Every API was verified against the pinned versions (Spring AI 2.0.0, LangChain4j 1.17.2,
Quarkus LangChain4j 1.12.2) rather than reproduced from the chapter.

## Ingestion is unchanged

The ingestion half is identical to `04-rag-classic`: a one-shot Camel route runs `tika:parse` to extract
the PDF to plain text, then a stack-native loader splits, embeds and stores it; collection creation is
automatic on Spring AI and manual on both LangChain4j stacks. See the classic README for that story. Only
**retrieval** changes here.

## The headline: declarative retrieval, three ways

| | Spring AI | LangChain4j (Boot) | LangChain4j (Quarkus) |
|---|---|---|---|
| mechanism | `QuestionAnswerAdvisor` registered on the `ChatClient` | an **AI Service** (`Assistant` interface) built with an `EmbeddingStoreContentRetriever` | `@RegisterAiService` interface + a produced `RetrievalAugmentor` bean |
| extra dependency | `spring-ai-vector-store-advisor` (not transitive) | none beyond the classic set | none beyond the classic set |
| where retrieval is declared | `chatClientBuilder.defaultAdvisors(advisor)` | `AiServices.builder(...).contentRetriever(...)` | a CDI `@Produces RetrievalAugmentor` bean, discovered by type |

Two findings are worth stating plainly.

**LangChain4j needs an `Assistant` abstraction that Spring AI does not.** Spring AI attaches the advisor
to the same imperative `ChatClient` the classic stack already used, so the shape barely changes.
LangChain4j's declarative retrieval is bound to its **AI Services** feature, so both LangChain4j stacks
introduce an `Assistant` interface (a `@SystemMessage` grounding instruction plus the question) that the
classic variant had no need for.

**Quarkus discovers a `RetrievalAugmentor`, not a bare `ContentRetriever`.** `@RegisterAiService` locates
its retrieval component by CDI type through the default supplier, which looks for a `RetrievalAugmentor`
bean. Producing a plain `ContentRetriever` bean leaves the assistant un-augmented and it answers from the
model's own memory. The retriever is therefore wrapped in a `DefaultRetrievalAugmentor`. On Spring Boot,
`AiServices.contentRetriever(...)` takes the `ContentRetriever` directly, so no wrapper is needed there.

## Declarative retrieval, side by side

Spring AI — register the advisor once, then just ask:

```java
var chatClient = chatClientBuilder
    .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().topK(4).build())
        .build())
    .build();
chatClient.prompt().user(question).call().content();   // retrieval happens inside the call
```

LangChain4j on Boot — declare the retriever on the AI Service:

```java
Assistant assistant = AiServices.builder(Assistant.class)
    .chatModel(chatModel)
    .contentRetriever(EmbeddingStoreContentRetriever.builder()
        .embeddingStore(store).embeddingModel(embeddingModel).maxResults(4).build())
    .build();
assistant.answer(question);
```

LangChain4j on Quarkus — a produced augmentor the `@RegisterAiService` assistant discovers:

```java
@Produces @ApplicationScoped
RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel) {
  return DefaultRetrievalAugmentor.builder()
      .contentRetriever(EmbeddingStoreContentRetriever.builder()
          .embeddingStore(store).embeddingModel(embeddingModel).maxResults(4).build())
      .build();
}
```

In every stack the RAG service becomes a one-line delegate: no `similaritySearch`, no manual prompt.

## Structure

```
04-rag-advisor/
  ├── 04-rag-advisor-spring-ai-boot/      # Spring AI: QuestionAnswerAdvisor on the ChatClient
  ├── 04-rag-advisor-langchain4j-boot/    # LangChain4j on Boot: AI Service + EmbeddingStoreContentRetriever
  ├── 04-rag-advisor-langchain4j-quarkus/ # LangChain4j on Quarkus: @RegisterAiService + produced RetrievalAugmentor
  └── docker-compose.yml                  # Ollama + Qdrant, to run a stack by hand
```

The contract, the corpus PDF (fictional company Aurora Labs) and `AbstractRagQuestionAnsweringIT` live in
`04-rag-common`. The test asks for facts stated **only** in the PDF (its HQ city, its product name) and
asserts the answer contains them, proving that the framework's retriever fed the generation, plus
input-validation probes.

## The models

Answering runs on the small `qwen2.5:0.5b`; embedding uses `nomic-embed-text` (768 dims), pulled lazily
so only RAG use cases pay for it. Both share one Ollama container per module.

## Status

| Track | State |
|---|---|
| `04-rag-advisor-spring-ai-boot` | done — `QuestionAnswerAdvisor`, passes the suite |
| `04-rag-advisor-langchain4j-boot` | done — AI Service with `EmbeddingStoreContentRetriever`, passes the suite |
| `04-rag-advisor-langchain4j-quarkus` | done — `@RegisterAiService` with a produced `RetrievalAugmentor`, passes the suite |

`mvn verify` runs each stack's RAG flow against one shared Ollama container (answering + embedding models)
and one shared Qdrant container per module.
