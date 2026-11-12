# Errata

This file records the corrections and the later developments that concern the published edition of the
book (Leanpub ebook, October 2026, and Amazon paperback, November 2026). The book pins LangChain4j 1.17.2,
quarkus-langchain4j 1.12.2, Spring AI 2.0.0 and Spring Boot 4. Every statement below refers to these versions
unless another version is named explicitly.

Entries are located by chapter and section title, because the ebook and the paperback paginate differently.

Dmytro Liubarskyi, initiator and maintainer of the LangChain4j project, reported the items listed here. I'd want to thank him for a careful and generous reading.

## Corrections

These entries correct statements that were inaccurate at the pinned versions.

### E1. Chapter 6, sections "Persistent: the sharpest asymmetry yet" and "Reading the findings"

Published text: LangChain4j has no persistent memory at all, and the only store it ships keeps messages in a
map.

Correction: LangChain4j 1.17.2 ships several persistent `ChatMemoryStore` implementations. The main
repository provides stores for Cassandra, Coherence, Oracle, Tablestore and Azure Cosmos DB NoSQL. The
`langchain4j-community` repository provides stores for Redis, MongoDB, SQL databases, Neo4j and Hazelcast.
The Quarkus extension provides Redis and MongoDB memory stores. LangChain4j provides two capabilities
exclusively through custom code: a memory that recalls past messages by similarity over a vector store, and
a memory store backed by Qdrant.

Consequence for the chapter: the asymmetry between the two libraries concerns semantic recall, which Spring AI
offers through `VectorStoreChatMemoryAdvisor`, and the choice of Qdrant as the storage engine. Durable storage
of the conversation window is available in both libraries. The hand-written `QdrantChatMemoryStore` remains a
valid illustration of the `ChatMemoryStore` extension point, since the book uses Qdrant throughout.

### E2. Chapter 10, sections "Turning it on, three ways" and "The meter you write by hand"

Published text: the LangChain4j core ships no Micrometer integration, so the listener that records the
`gen_ai.*` meters on Spring Boot has to be written by hand.

Correction: the `langchain4j-micrometer-metrics` module, available in 1.17.2, provides
`MicrometerMetricsChatModelListener`. This listener records `gen_ai.client.operation.duration` and
`gen_ai.client.token.usage`, which are the metric names that the chapter wires by hand. Spring Boot provides
no auto-configuration for this module, so the application registers the listener as a bean. Writing the
listener class itself is therefore unnecessary. The hand-written `TokenUsageChatModelListener` remains a correct
illustration of the `ChatModelListener` extension point.

### E3. Chapter 13, section "Agents are something you write"

Published text: LangChain4j on Quarkus at its current release has no agentic API.

Correction: quarkus-langchain4j 1.12.2 contains the `quarkus-langchain4j-agentic` extension, which integrates
`langchain4j-agentic` with Quarkus. The section "What is coming for the JVM" of the same chapter states this
correctly. The documentation is available at
<https://docs.quarkiverse.io/quarkus-langchain4j/dev/agentic.html>.

### E4. Chapter 6, the note on CDI scope and the section "Reading the findings"

Published text: the note and the summary explain that a `@RegisterAiService` interface is `@RequestScoped`
by default, and that the in-memory Quarkus variant therefore requires `@ApplicationScoped`.

Clarification: this explanation is correct, and it identifies the cause of the behavior observed while
writing the use case. The repository README of use case 05 had reported that `@RegisterAiService` did not wire
chat memory at all, and the Quarkus variants build their AI service manually as a consequence. According to
the LangChain4j maintainer, an `@ApplicationScoped` AI service combined with a `@MemoryId` parameter makes the
automatic memory work as documented at
<https://docs.quarkiverse.io/quarkus-langchain4j/dev/messages-and-memory.html>. The repository will be updated
once this configuration has been verified against the conformance suite.

### E5. Chapter 2, table "Weight and startup of the three stacks for use case 01"

Published text: LangChain4j on Spring Boot starts in 2.69 s.

Correction: this figure was measured on an earlier version of the module, which declared the `ChatModel`
bean in a hand-written `ChatModelConfig` class. The published module relies on
`langchain4j-ollama-spring-boot4-starter` instead, as the chapter describes. The figure therefore describes
code that the book no longer presents, and it overstates the startup difference between the two Spring Boot
stacks. A new measurement will be published here. The same figure appears in the companion article.

## Developments since publication

These entries concern statements that were accurate at the pinned versions and that later releases have
changed. They will be integrated into the use cases when the repository moves to newer versions.

### D1. Chapter 9, section "Where the stacks differ"

The book states that text to speech is available on Spring AI only. LangChain4j 1.18.0 introduced
`TextToSpeechModel` and its OpenAI implementation `OpenAiTextToSpeechModel`, so the `/speak` endpoint can be
implemented on the two LangChain4j stacks from that version onward.

### D2. Chapter 11, section "Authorizing a tool call"

The book states that `@PreAuthorize` on a LangChain4j `@Tool` method makes the tool invisible, because Spring
wraps the bean in a CGLIB proxy whose methods carry no `@Tool` annotation. LangChain4j 1.20.0 discovers
`@Tool` methods on proxied beans, so the declarative authorization used on the Spring AI stack also applies to
LangChain4j on Spring Boot from that version onward.

### D3. Chapter 8, section "Transports, and where they disagree"

The repository documents that the Quarkus MCP client hangs against a stateful Streamable HTTP server, which
is the reason the use case runs its MCP server in stateless mode. The defect, reported by the author, was fixed
upstream in quarkus-langchain4j pull request 2872. The stateless server remains a sound choice for a tool
server, and the interoperability table in the use case README will be revised after the upgrade.
