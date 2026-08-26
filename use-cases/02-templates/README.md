# 02 — Templates, injected context, and structured output

Use case 01 forwarded a raw question and returned raw text. This one adds the three things almost
every real AI feature needs next, and nothing more:

1. a **templated system prompt** — the instructions are a file with placeholders, not a string literal;
2. **optional injected context** — if the application carries a reference document for the topic, it
   is folded into the prompt; if not, the model answers from general knowledge;
3. **structured output** — the model returns a typed `{topic, answer}` record, not a bare string, and
   each call logs its token usage.

```shell
curl localhost:8080/ask -H 'Content-Type: application/json' \
     -d '{"topic":"Aurora Labs","question":"In which city is it headquartered?"}'
```

```json
{"topic":"Aurora Labs","answer":"Aurora Labs is headquartered in Reykjavik."}
```

`Aurora Labs` is a fictional company described in [`reference/aurora_labs.txt`](02-templates-common/src/main/resources/reference/aurora_labs.txt).
A base model cannot know it, so a correct answer *proves* the injected context reached the model —
that is exactly what the conformance suite checks. Ask about a topic with no reference file (say
`France`) and the same endpoint answers from general knowledge instead.

## Where it came from

[`_original/`](_original) is chapter *Board Game Buddy* from *Spring AI in Action*: a `/ask` endpoint
that looks up a game's rules, folds them into a templated prompt, and returns a structured answer.
As in use case 01, the **board-game domain is dropped** — the mechanism is the point, not the domain —
so the app accepts any `topic` and treats its reference lookup as optional.

### Renames

| `_original` | Converted | Why |
|---|---|---|
| `BoardGameBuddyApplication` | `QuestionAnsweringApplication` | names what the application does |
| `BoardGameService` | `QuestionAnsweringService` | the shared, neutral interface |
| `SpringAiBoardGameService` | `SpringAiQuestionAnsweringService` | keeps the `<Stack><Role>` pattern |
| `GameRulesService` | `ReferenceService` | topic-neutral context lookup, not game rules |
| `Question(gameTitle, question)` | `Question(topic, question)` | topic is any subject |
| `Answer(gameTitle, answer)` | `Answer(topic, answer)` | the structured-output target |
| `gameRules/burger_battle.txt` | `reference/aurora_labs.txt` | a neutral, fictional reference doc |
| `com.example.boardgamebuddy` | `fr.simplex_software.workshop.templates.*` | project convention |

### Other deliberate changes

**OpenAI became Ollama**, as in 01 — no API key, reproducible in CI, supported equally by all three
stacks. **Validation was added**: `Question` carries `@NotBlank` on both fields, so a blank topic or
question returns 400 without reaching the model. **The mock-OpenAI test was dropped** — it stubs
OpenAI's HTTP API and cannot survive the move to Ollama, and it is mock-based where this project
prefers real integration tests.

### One widened boundary, on purpose

Use case 01 keeps `common` to DTOs, the REST contract, and the conformance test. Here `common` also
carries the **reference corpus and a neutral `ReferenceService`** (plain classpath I/O — no Spring, no
CDI, no AI). The reason is the same one that motivates the shared conformance test: if all three stacks
read the *identical* document, the "same behaviour, three stacks" claim stays honest. Prompt
construction — which genuinely differs per stack — stays out of `common`.

## The implementations

The interesting divergence is **how each stack templates the prompt and gets a typed object back**.

### Spring AI on Spring Boot — one fluent chain

Templating, context, structured output and token logging are a single call chain. The template is a
`.st` resource; `responseEntity(Answer.class)` both asks for and parses the structured result:

```java
var responseEntity = chatClient.prompt()
    .system(system -> system
        .text(systemPromptTemplate)                 // classpath .st resource
        .param("topic", question.topic())
        .param("reference", reference))
    .user(question.question())
    .call()
    .responseEntity(Answer.class);                  // structured output

logUsage(responseEntity.response().getMetadata().getUsage());
return responseEntity.entity();
```

### LangChain4j on Spring Boot — a declarative AI service

LangChain4j inverts it: the prompt *is* the interface. `@SystemMessage` points at a template file,
`@V` binds the variables, the return type drives structured output, and `Result<Answer>` also carries
token usage:

```java
public interface Assistant {
  @SystemMessage(fromResource = "prompts/systemPromptTemplate.txt")
  Result<Answer> answer(@V("topic") String topic,
                        @V("reference") String reference,
                        @UserMessage String question);
}
```

But — as in use case 01 — LangChain4j has no working Spring Boot 4 starter, so the model and the
service are wired by hand, and this is where structured output is switched on for a tiny model:

```java
@Bean
ChatModel ollamaChatModel(...) {
  return OllamaChatModel.builder()
      .baseUrl(baseUrl).modelName(modelName).temperature(temperature)
      .responseFormat(ResponseFormat.JSON)
      .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)   // grammar-constrained JSON
      .build();
}

@Bean
Assistant assistant(ChatModel chatModel) {
  return AiServices.builder(Assistant.class).chatModel(chatModel).build();
}
```

### LangChain4j on Quarkus — the same service, none of the wiring

The interface is identical in spirit, but `@RegisterAiService` wires the auto-configured Ollama model
for you — there is no `ChatModelConfig`, and structured output is one property, not two builder calls:

```java
@RegisterAiService
public interface Assistant {
  @SystemMessage("""
    You are a helpful assistant answering a question about {topic}.
    ... Set the "topic" field of your answer to: {topic}
    REFERENCE
    ---------
    {reference}
    """)
  Result<Answer> answer(@V("topic") String topic,
                        @V("reference") String reference,
                        @UserMessage String question);
}
```

```properties
quarkus.langchain4j.ollama.chat-model.format=json
```

## The conformance suite

[`02-templates-common`](02-templates-common) defines `AbstractQuestionAnsweringIT`; every stack extends
it and points it at its own running application. It POSTs to `/ask` and asserts:

1. **grounded** — asking about `Aurora Labs` returns 200, echoes the `topic`, and the `answer` contains
   `reykjavik` (only obtainable from the injected reference: proof the template + context worked);
2. **neutral** — asking about `France` with no reference returns 200 and an `answer` containing `paris`;
3. a blank question → 400;  4. a blank topic → 400;  5. a malformed payload → 400.

Assertions are semantic, never exact-string. The `topic` echo doubles as the structured-output check:
if the field is populated, the model returned the shape, not just prose.

## Metrics

Lines are non-blank, non-comment, non-import. All three stacks pass the identical five conformance
assertions plus one Docker-free wiring test.

| | `_original` | spring-ai-boot | langchain4j-boot | langchain4j-quarkus |
|---|---|---|---|---|
| Production Java (LOC) | 79 | **55** | 71 | 51 |
| …the AI service class | 27 | 33 | 19 | 19 |
| …wiring the model | 0 | 0 | **23** | 0 |
| …the AI-service interface | n/a | 0 | 7 | 16 |
| Files in `src/main/java` | 7 | **3** | 5 | 3 |
| `<dependency>` entries in the leaf POM | 5 | 3 | **4** | 3 |
| Conformance glue (LOC) | n/a | 24 | 22 | 20 |

`02-templates-common` adds 28 lines of production Java — the two records, the interface and the
`ReferenceService` — plus the reference corpus and the conformance suite, paid once for all three
stacks.

**LangChain4j on Boot is the only stack that grows a file and a dependency.** It needs the `Assistant`
interface *and* `ChatModelConfig` *and* the extra `dev.langchain4j:langchain4j` dependency (AiServices
lives there, not in `langchain4j-ollama`). Spring AI folds everything into one class; Quarkus needs the
interface but no config and no extra dependency.

## Where they genuinely diverge

### Structured output: one call, one builder pair, or one property

All three reach the same result — a parsed `Answer` from `qwen2.5:0.5b` — by different routes:

| | how structured output is requested |
|---|---|
| Spring AI | `.responseEntity(Answer.class)` on the call chain |
| LangChain4j / Boot | POJO return type **+** `responseFormat(JSON)` **+** `RESPONSE_FORMAT_JSON_SCHEMA` capability on the model |
| LangChain4j / Quarkus | POJO return type **+** `quarkus.langchain4j.ollama.chat-model.format=json` |

The headline finding of use case 02: **a 0.5b model returns reliable structured JSON on every stack**,
because each ultimately drives Ollama's grammar-constrained output. It is not automatic on LangChain4j —
omit the capability (Boot) or the `format` property (Quarkus) and a tiny model will eventually emit
prose that fails to parse. Spring AI turns it on implicitly via `responseEntity`.

### Prompt templating: `{x}`, `{{x}}`, and where the template lives

| | placeholder | template location |
|---|---|---|
| Spring AI | `{topic}` | external `.st` resource, referenced by `@Value` |
| LangChain4j / Boot | `{{topic}}` | external resource, referenced by `@SystemMessage(fromResource=…)` |
| LangChain4j / Quarkus | `{topic}` | inline in the `@SystemMessage` annotation |

Spring AI and Quarkus use single braces; LangChain4j-on-Boot uses double. Same three variables, three
spellings.

### `@V` is mandatory on Quarkus, not optional

On Quarkus the build-time `AiServicesProcessor` maps template variables to method parameters. Without
`@V("topic")` it cannot recover parameter names (the build does not compile with `-parameters`) and
fails at build time with `Duplicate key null`. The `@V` annotations are therefore load-bearing on
Quarkus, where on plain LangChain4j they are a convenience.

### Token usage: metadata vs. `Result`

Spring AI exposes usage through `ChatResponse.getMetadata().getUsage()` (prompt / completion / total).
LangChain4j returns it as part of `Result<T>.tokenUsage()` (input / output / total) — same numbers,
different names, and on LangChain4j you opt in by widening the return type from `Answer` to
`Result<Answer>`.

### Above the service layer, still nothing changes

As in 01, `AskController` (Spring) and `AskResource` (JAX-RS) are unchanged by the choice of AI library
or the addition of structured output — the typed `Answer` crosses the HTTP boundary identically on all
three. The divergence is entirely inside the service and its prompt.

## Status

| Track | State |
|---|---|
| `02-templates-common` | done — contract, reference corpus + conformance suite |
| `02-templates-spring-ai-boot` | done — passes the conformance suite |
| `02-templates-langchain4j-boot` | done — passes the conformance suite |
| `02-templates-langchain4j-quarkus` | done — passes the conformance suite |

`mvn verify` builds every module and runs the suite against one shared Ollama container per stack.
