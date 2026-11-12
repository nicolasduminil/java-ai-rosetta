# 05 — Conversational memory

Use cases 01–04 answered one question at a time. This one **remembers the conversation**: a `/chat`
endpoint takes a `conversationId` with each question, recalls what was said earlier in that conversation,
and keeps separate conversations isolated. It is built two ways — **in memory** (a bounded message window
per conversation) and **persisted to Qdrant** — across the three stacks.

```shell
curl localhost:8080/chat -H 'Content-Type: application/json' \
     -d '{"conversationId":"c1","question":"My name is Aurora. Remember it."}'
curl localhost:8080/chat -H 'Content-Type: application/json' \
     -d '{"conversationId":"c1","question":"What is my name? Answer with the name only."}'
# -> {"answer":"Aurora"}
```

## Structure

A single aggregate with the shared contract nested alongside the six stack modules (two memory flavours ×
three stacks):

```
05-memory/
  ├── 05-memory-common/                       # Question/Answer/ConversationService + conformance test (test-jar)
  ├── 05-memory-inmemory-spring-ai-boot/       # MessageChatMemoryAdvisor
  ├── 05-memory-inmemory-langchain4j-boot/     # AiServices + ChatMemoryProvider
  ├── 05-memory-inmemory-langchain4j-quarkus/  # hand-built AiServices + ChatMemoryProvider
  ├── 05-memory-persistent-spring-ai-boot/     # VectorStoreChatMemoryAdvisor over Qdrant
  ├── 05-memory-persistent-langchain4j-boot/   # hand-rolled Qdrant ChatMemoryStore
  └── 05-memory-persistent-langchain4j-quarkus/# same store, CDI-wired
```

## Where it came from

Adapted **by description** from chapter *Enabling conversational memory* of *Spring AI in Action*
(Craig Walls). As with 01–04 the book's domain is dropped and only the mechanism is kept; every API was
verified against the pinned versions (Spring AI 2.0.0, LangChain4j 1.17.2, Quarkus LangChain4j 1.12.2).

## In-memory: three stacks

| | Spring AI | LangChain4j (Boot) | LangChain4j (Quarkus) |
|---|---|---|---|
| memory component | `MessageChatMemoryAdvisor` on the `ChatClient` | `ChatMemoryProvider` on an `AiServices` assistant | same, built by hand |
| the window | `MessageWindowChatMemory` + `InMemoryChatMemoryRepository` | `MessageWindowChatMemory` (default `InMemoryChatMemoryStore`) | same |
| conversation id | `.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, id))` | `@MemoryId` method parameter | `@MemoryId` method parameter |

## Persistent: the headline asymmetry

| | Spring AI 2.0.0 | LangChain4j (Boot & Quarkus) |
|---|---|---|
| persistent memory | semantic recall: `VectorStoreChatMemoryAdvisor` over a `VectorStore` | durable `ChatMemoryStore` implementations (Cassandra, Coherence, Oracle, Tablestore, Cosmos DB, and Redis, MongoDB, SQL, Neo4j, Hazelcast in `langchain4j-community`). Similarity recall and a Qdrant store are custom code |
| how it works | each turn is embedded and stored; the next call retrieves the most **similar** earlier turns (semantic top-K) | a hand-rolled `ChatMemoryStore` stores/reloads the **whole** message window verbatim |
| extra dependency | `spring-ai-vector-store-advisor` (not transitive) | the raw Qdrant client, for the custom store |

Spring AI ships a vector store memory advisor that retrieves the past messages most similar to the current
turn. LangChain4j ships durable `ChatMemoryStore` implementations for many databases and leaves similarity
recall and Qdrant storage to the application. Since every use case of this repository stores its vectors in
Qdrant, the LangChain4j stacks implement a `ChatMemoryStore` over Qdrant, with one Qdrant point per conversation,
the serialized message window in its payload, a one-dimension placeholder vector that is never searched.

## Two findings worth stating plainly

**`PromptChatMemoryAdvisor` is gone in Spring AI 2.0.0.** The book (built on an earlier milestone) presents
`MessageChatMemoryAdvisor`, `PromptChatMemoryAdvisor` and `VectorStoreChatMemoryAdvisor`. In 2.0.0 the
`Prompt…` variant has been removed; only the `Message…` (in-memory) and `VectorStore…` (persistent) advisors survive.

Quarkus chat memory follows the CDI scope of the AI service (1.12.2). A `@RegisterAiService` interface is
`@RequestScoped` by default, so its automatic memory lasts for a single HTTP request, and the second turn of a
conversation starts from an empty memory. The in-memory Quarkus stack therefore declares its `@RegisterAiService`
interface `@ApplicationScoped` and names the conversation with a `@MemoryId` parameter, as described at
<https://docs.quarkiverse.io/quarkus-langchain4j/dev/messages-and-memory.html>. The conformance suite confirms
the role of the scope: with the annotation removed, the recall test fails, because each request receives a
new, empty memory. The persistent Quarkus stack builds its AI service with
`AiServices.builder(...).chatMemoryProvider(...)` around the custom `QdrantChatMemoryStore`, the same
construction as the Spring Boot stacks. See [`ERRATA.md`](../../ERRATA.md), entries E1 and E4.

## The conformance suite

`05-memory-common` defines `AbstractConversationalMemoryIT`; all six stacks run it unchanged. It POSTs to
`/chat` and asserts:

1. **recall** — telling the assistant a name in a conversation, then asking for it in the same conversation,
   returns the name (`aurora`);
2. **isolation** — asking in a *different* conversation does not return it;
3. a blank question → 400;  4. a blank conversation id → 400;  5. a malformed payload → 400.

Assertions are semantic (a single grounded token), never exact-string. Persistence is shown **by
construction** (the store is wired to Qdrant) and documented here, not gated by an app-restart test — the
same "assert the capability, document the rest" rule as 03-eval and RAG. (Note too that Spring AI's
persistent recall is *semantic* top-K, not a full transcript, so a transcript-equality test would not even
be meaningful across the two libraries.)

## Status

All six stacks pass the identical conformance suite against one shared Ollama container (`qwen2.5:0.5b`;
plus `nomic-embed-text` for Spring AI's embedded persistent memory) and, for the persistent stacks, one
shared Qdrant container per module. `mvn verify` runs the lot.
