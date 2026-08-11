# 01 — Simple question answering

The smallest thing an AI application can be: one endpoint that forwards a question to a model and
returns the answer.

```shell
curl localhost:8080/ask -H 'Content-Type: application/json' \
     -d '{"question":"Why is the sky blue?"}'
```

```json
{"answer":"Because sunlight scatters..."}
```

There is no retrieval, no memory, no tools, no structured output. That is deliberate: it isolates
the one thing every AI framework must do — build a prompt, call a model, hand back text — so the
comparison starts from the smallest possible surface.

## Status

| Track | State |
|---|---|
| `01-simple-common` | done — wire contract + conformance suite |
| `01-simple-spring-ai-boot` | done — passes the conformance suite |
| `01-simple-langchain4j-boot` | done — passes the conformance suite |
| `01-simple-langchain4j-quarkus` | done — passes the conformance suite |

All three pass the identical three assertions in `AbstractQuestionAnsweringIT`, against one shared
Ollama container, and each carries one Docker-free wiring test besides. Same four tests per stack,
so the counts in the comparison mean the same thing. `mvn verify` runs the lot.

## Where it came from

[`_original/`](_original) holds the Gradle sources exactly as provided: chapter 1 of *Spring AI in
Action*, an application called **Board Game Buddy**. Nothing in the code is about board games — the
service forwards the question verbatim and the name is aspirational for later chapters — so the
converted version drops it.

### Renames

| `_original` | Converted | Why |
|---|---|---|
| `BoardGameBuddyApplication` | `QuestionAnsweringApplication` | names what the application does |
| `BoardGameService` | `QuestionAnsweringService` | same, and it is the shared interface now |
| `SpringAiBoardGameService` | `SpringAiQuestionAnsweringService` | keeps the `<Stack><Role>` pattern |
| `AskController` | `AskController` | already accurate |
| `Question`, `Answer` | unchanged | already framework- and domain-neutral |
| `com.example.boardgamebuddy` | `fr.simplex_software.workshop.simple.*` | project convention |
| root project `board-game-buddy` | `01-simple-*` | use-case-scoped module naming |

The package splits by role: `…simple.api` in the common module holds the contract, `…simple.springai`
holds this stack's implementation. Each further stack gets its own leaf package, so all three can
sit in one IDE without colliding.

### Other deliberate changes

**OpenAI became Ollama.** The original reads `SPRING_AI_OPENAI_API_KEY` and calls `gpt-3.5-turbo`.
Every track here runs against an Ollama container instead, so the suite needs no API key, costs
nothing, and is reproducible in CI — and because all three stacks support Ollama equally, the
comparison stays apples-to-apples. This is the single largest departure from `_original`.

**A blank question is now rejected.** `Question` carries `@NotBlank` and `AskController` validates
it, so an empty question returns 400 instead of being billed to the model. The original had no
validation and no negative test; the project convention requires both. Being on the shared DTO, the
rule binds every stack equally.

**`AnswerNotRelevantException` was dropped.** It is present in `_original` but referenced by
nothing — a hook for a later chapter of the book. Dead on arrival here.

**Three of the four original tests were dropped.** `SpringAiBoardGameServiceMockOpenAiTests` and
`SpringAiBoardGameServiceWireMockTests` stub OpenAI's HTTP API, so they cannot survive the move to
Ollama and, being mock-based, run against the project's preference for real integration tests.
`BoardGameBuddyApplicationTests` (`contextLoads`) survives as `QuestionAnsweringApplicationTests`.
`SpringAiBoardGameServiceTests`, which judges answers with Spring AI's `RelevancyEvaluator` and
`FactCheckingEvaluator`, is the interesting one — see the open question below.

## The implementations

### Spring AI on Spring Boot

The whole AI surface of this stack is one constructor and one call chain:

```java
@Service
public class SpringAiQuestionAnsweringService implements QuestionAnsweringService {

  private final ChatClient chatClient;

  public SpringAiQuestionAnsweringService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Answer askQuestion(Question question) {
    var answerText = chatClient.prompt()
        .user(question.question())
        .call()
        .content();
    return new Answer(answerText);
  }
}
```

The starter auto-configures `ChatClient.Builder` from `spring.ai.ollama.*`, so no endpoint, model
or transport appears in Java at all — it is entirely in `application.properties`:

```properties
spring.ai.ollama.chat.options.model=${ollama.model:qwen2.5:0.5b}
spring.ai.ollama.chat.options.temperature=0.0
spring.ai.ollama.init.pull-model-strategy=never
```

### LangChain4j on Spring Boot

The call itself is shorter than Spring AI's — no fluent chain, just a method:

```java
@Service
public class LangChain4jQuestionAnsweringService implements QuestionAnsweringService
{
  private final ChatModel chatModel;

  public LangChain4jQuestionAnsweringService(ChatModel chatModel)
  {
    this.chatModel = chatModel;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(chatModel.chat(question.question()));
  }
}
```

But this track needs a class the other two do not — see *The starter that isn't there* below:

```java
@Configuration
public class ChatModelConfig
{
  @Bean
  ChatModel ollamaChatModel(
    @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
    @Value("${langchain4j.ollama.chat-model.model-name}") String modelName,
    @Value("${langchain4j.ollama.chat-model.temperature}") Double temperature)
  {
    var builder = OllamaChatModel.builder();
    builder.baseUrl(baseUrl).modelName(modelName).temperature(temperature);
    return builder.build();
  }
}
```

### LangChain4j on Quarkus

The service body is character-for-character the same as the Boot one; only the scope annotation
changes, `@ApplicationScoped` for `@Service`. And here the model *is* auto-configured, so there is
no `ChatModelConfig` — this is the smallest of the three modules:

```java
@ApplicationScoped
public class LangChain4jQuestionAnsweringService implements QuestionAnsweringService
{
  private final ChatModel chatModel;
  // ... identical to the Spring Boot version
}
```

The HTTP layer is JAX-RS rather than Spring MVC, but the wire contract is identical:

```java
@Path("/ask")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AskResource
{
  @POST
  public Answer ask(@Valid Question question)
  {
    return questionAnsweringService.askQuestion(question);
  }
}
```

## The conformance suite

[`01-simple-common`](01-simple-common) defines the contract in
`AbstractQuestionAnsweringIT`. It POSTs to `/ask` with REST Assured and asserts:

1. a factual question returns 200 and an answer containing the expected fact;
2. a blank question returns 400 and never reaches the model;
3. a malformed payload returns 400.

Assertions are semantic, never exact-string — an LLM's wording is not reproducible even at
temperature zero, so the positive case pins a token any usable model produces rather than a
sentence. Everything a stack must supply is the base URI of its running application:

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SpringAiQuestionAnsweringIT extends AbstractQuestionAnsweringIT {

  @LocalServerPort private int port;

  @DynamicPropertySource
  static void ollamaProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.ai.ollama.base-url", OllamaTestSupport::baseUrl);
    registry.add("spring.ai.ollama.chat.options.model", OllamaTestSupport::model);
  }

  @Override protected URI baseUri() { return URI.create("http://localhost:" + port); }
}
```

Twenty-five lines is the entire cost of joining the contract. That number is itself a metric: the
more a stack needs here, the less the "same test, three stacks" claim is worth.

## Metrics

Lines are non-blank, non-comment. Runtime dependencies are the resolved `runtime`-scope jars, which
is what actually ships.

| | `_original` | spring-ai-boot | langchain4j-boot | langchain4j-quarkus |
|---|---|---|---|---|
| Production Java (LOC) | 59 | 48 | 66 | **39** |
| …of which the AI service class | 19 | 21 | 18 | 18 |
| …of which wiring the model | 0 | 0 | **19** | 0 |
| Files in `src/main/java` | 7 | 3 | 4 | **2** |
| Dependencies declared in the build file | 5 | 3 | 3 | 3 |
| Resolved runtime jars | — | 106 | **45** | 142 |
| Conformance glue (LOC) | n/a | 25 | 23 | 36 |
| Startup, median (JVM mode) | — | 1.74 s | 2.69 s | **0.66 s** |
| Native image | n/a | not attempted | not attempted | profile present, not attempted |

The shared `01-simple-common` module adds 14 lines of production Java (the two records and the
interface) and 83 of conformance test — 51 for the contract, 32 for the Ollama fixture — paid once
for all three stacks.

**How startup was measured.** Median of 7 runs each for the Boot tracks and 3 for Quarkus, JVM mode,
on one machine, against an already-running Ollama container so that only application boot is inside
the window. This matters: the figures the conformance suite logs are 30–40 s, because the container
start and model pull happen *inside* the measured startup. Those numbers are meaningless as a
comparison and are not the ones above.

**Read the jar counts with care.** Quarkus splits its runtime into many small artifacts, so 142
jars is not 3× the weight of 45. The comparison worth making is Spring AI's 106 against
LangChain4j's 45 on the *same* runtime: Spring AI brings a JSON-schema generator, a tokenizer and
Reactor along with it, and this use case touches none of them.

**Conformance glue is 36 lines on Quarkus** rather than ~24, because the shared container is fed in
through a `QuarkusTestResourceLifecycleManager` (18 lines) instead of a one-line
`@DynamicPropertySource` registration. Same fixture, more ceremony.

## Where they genuinely diverge

### The starter that isn't there

**LangChain4j has no Spring Boot 4 starter, and this is the headline finding of use case 01.**

`langchain4j-ollama-spring-boot-starter:1.17.2-beta27` pulls
`langchain4j-http-client-spring-restclient`, compiled against
`org.springframework.boot.http.client.ClientHttpRequestFactorySettings` — a class Spring Boot 4
removed. The application does not fail to compile; it fails at bean creation, at runtime:

```
NoClassDefFoundError: org/springframework/boot/http/client/ClientHttpRequestFactorySettings
  at dev.langchain4j.http.client.spring.restclient.SpringRestClient.<init>
```

The fix is to skip the starter: take `langchain4j-ollama` plus `langchain4j-http-client-jdk` and
declare the `ChatModel` bean yourself. That is the 19-line `ChatModelConfig` in the metrics table —
the only hand-written wiring in any of the three tracks. Note the direction of the asymmetry:
**Spring AI runs on Spring Boot 4 out of the box; LangChain4j needs help on Spring's own runtime,
while its Quarkus extension auto-configures cleanly.**

The property names in `ChatModelConfig` are deliberately the ones the starter would use, so
adopting it when a Boot 4 version ships means deleting the class and changing nothing else.

### Three names for the same two settings

| | base URL | model |
|---|---|---|
| Spring AI | `spring.ai.ollama.base-url` | `spring.ai.ollama.chat.model` |
| LangChain4j / Boot | `langchain4j.ollama.chat-model.base-url` | `…chat-model.model-name` |
| LangChain4j / Quarkus | `quarkus.langchain4j.ollama.base-url` | `…ollama.chat-model.model-id` |

Note LangChain4j itself is not consistent between its own two integrations: on Boot the base URL
nests under `chat-model`, on Quarkus it sits at the extension root, and the model is `model-name`
in one and `model-id` in the other.

### Optional versus mandatory configuration

Spring AI's Ollama starter defaults its base URL, so an application with no configuration at all
still starts. The LangChain4j Boot starter guards its bean with
`@ConditionalOnProperty("langchain4j.ollama.chat-model.base-url")` — omit that one line and there
is no `ChatModel`, no service, and no application context. Neither behaviour is wrong; they fail in
opposite directions, one toward a working default and one toward an explicit error.

### Above the service layer, nothing changes

`AskController` is byte-identical between the two Spring Boot tracks apart from its package, and
`AskResource` differs only in being JAX-RS. **The choice of AI library is invisible above the
service class.** That is worth stating plainly, because it is the strongest argument that these
libraries are substitutable for this use case — and the reason the conformance suite can be blind
to which stack it is testing.

### Quarkus starts 2.6× faster, and is the smallest module

0.66 s against 1.74 s and 2.69 s, and 39 lines of production Java against 48 and 66. It is also the
only track with a native-image profile already wired in, inherited from its stack parent — though
native was not attempted here.

Some of the Boot/LangChain4j startup gap is likely self-inflicted: `ChatModelConfig` constructs the
`OllamaChatModel` eagerly at context refresh, which builds an HTTP client, whereas Spring AI's
`ChatClient.Builder` defers more. That explanation is plausible but **unverified** — it would need
profiling to claim properly.

### One porting trap

`jakarta.validation.Path` and `jakarta.ws.rs.Path` collide. Wildcard-importing both packages in a
JAX-RS resource fails to compile with `reference to Path is ambiguous`. The Spring tracks never hit
it, because Spring MVC has no `Path` annotation.

## Still open

**The dropped evaluator tests are the most interesting thing in `_original`.** Spring AI ships
`RelevancyEvaluator` and `FactCheckingEvaluator` — an LLM-as-judge API in the framework itself. The
conformance suite cannot use them: it must stay framework-neutral, and LangChain4j has no direct
equivalent. Both constructors still exist in Spring AI 2.0.0, so the option is open to keep a
stack-specific evaluator test in `01-simple-spring-ai-boot` as a genuine point of difference. Not
done yet — it is a decision, not an oversight.

**A 0.5b model is a weak judge but a fine subject.** `qwen2.5:0.5b` answers "capital of France"
reliably, which is why the positive assertion is shaped the way it is. Anything requiring the model
to reason would make the suite flaky, and a flaky conformance suite proves nothing. Use cases that
need a stronger model should raise `ollama.model` in the root POM for every stack at once.
