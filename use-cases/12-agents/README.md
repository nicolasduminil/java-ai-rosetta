# 12 — Agents (agentic workflow patterns)

The last use case: four agentic-workflow patterns, implemented on all three stacks and held to one
conformance test.

| Pattern | Endpoint | What it does |
|---|---|---|
| Prompt chaining | `POST /chain` | sequential steps: extract facts → tighten to a summary → write a headline |
| Routing | `POST /route` | classify the request (`BILLING`/`TECHNICAL`/`GENERAL`), then dispatch to a specialist |
| Parallelization | `POST /analyze` | fan out summary, sentiment and key points concurrently, then aggregate |
| Self-planning | `POST /plan` | a tool-calling loop: the model calls a `catalog` tool as needed to reach a goal |

Request bodies: `{"text": "..."}` for chain/route/analyze, `{"goal": "..."}` for plan. Everything runs on a
local Ollama model (`qwen2.5:3b`, `temperature=0`), with no key and no vector store.

## The Rosetta comparison

The headline finding is what is **not** there: at stable versions, none of the three stacks ships an
agentic-workflow API. The four patterns are hand-composed application code everywhere. What differs is the
orchestration idiom, especially for the concurrent fan-out.

| | Spring AI | LangChain4j on Boot | LangChain4j on Quarkus |
|---|---|---|---|
| Building block | imperative `ChatClient` calls | declarative `@AiService` interface | declarative `@RegisterAiService` interface |
| Chaining | sequential calls | sequential calls | sequential calls |
| Routing | wrapper-record classify + `switch` | **bare `Category` enum** + `switch` | wrapper-record classify + `switch` |
| Parallelization | Java 21 virtual-thread `Executor` | `CompletableFuture` (virtual threads) | **Mutiny `Uni.combine`** on `@Blocking` |
| Self-planning | `@Tool` + `ChatClient.tools()` loop | `@Tool` + `AiServices…tools()` loop | `@RegisterAiService(tools=…)` loop |

### Findings

- **Agents are orchestration you write, not a framework feature.** Spring AI 2.0.0 has no agent/workflow API
  at all (the `*Workflow` classes in its docs are sample code, not library types). quarkus-langchain4j 1.12.2
  has none either. So all four patterns are plain Java over the frameworks' building blocks: a model call,
  structured-output classification, and the tool-calling loop.
- **LangChain4j does have a first-class agentic module, but it is beta.** `dev.langchain4j:langchain4j-agentic`
  (aligned to core as `1.17.2-beta27`) offers `AgenticServices.sequenceBuilder`/`parallelBuilder`/
  `conditionalBuilder`/`supervisorBuilder`/`plannerBuilder`. It has no GA release and no Quarkus/CDI
  integration, so this use case keeps to the stable, hand-composed approach and covers the module in the
  chapter's forward-looking close, alongside the emerging Jakarta Agentic AI standard.
- **The concurrency model is the real per-stack delta.** The two Boot stacks fan out with imperative Java
  concurrency (a virtual-thread `Executor`; `CompletableFuture`). Quarkus is reactive: each blocking analysis
  is wrapped in a `Uni` run on the worker pool and combined with `Uni.combine()`, and because the result is
  awaited the endpoints are `@Blocking`.
- **Structured output splits the same way as in use case 11.** Routing needs a classification: raw
  LangChain4j on Boot returns the bare `Category` enum, while the JSON-path stacks (Spring AI, and Quarkus)
  wrap it in a one-field record. On Quarkus, because plain-`String` steps and JSON mode do not mix, the
  key-points step returns text that the orchestrator splits, rather than a list record.

## Running

```bash
cd use-cases/12-agents
# make sure Ollama is running locally (the apps default to http://localhost:11434), then:
ollama pull qwen2.5:3b
# then run any stack (each pinned to a fixed port), in its own terminal:
mvn -pl 12-agents-spring-ai-boot spring-boot:run        # :8080
mvn -pl 12-agents-langchain4j-boot spring-boot:run      # :8081
mvn -pl 12-agents-langchain4j-quarkus quarkus:dev       # :8082
```

Example calls:

```bash
curl -s localhost:8080/route -H 'Content-Type: application/json' \
  -d '{"text":"I was charged twice for my subscription and want a refund."}'
# -> {"category":"BILLING","answer":"..."}

curl -s localhost:8080/plan -H 'Content-Type: application/json' \
  -d '{"goal":"Use the catalog tool to look up the price of the WidgetPro."}'
# -> {"answer":"The WidgetPro costs 499 euros."}
```

## Testing

`mvn verify` runs the shared `AbstractAgentsIT` against an Ollama Testcontainer (`qwen2.5:3b`, no key) on each
stack: chaining returns a non-blank headline and summary; a billing complaint routes to `BILLING`;
parallelization returns a report with a summary, a sentiment and key points; self-planning answers a
tool-only question with the tool's value (`499`); blank or malformed input is rejected with 400. The
assertions check the shape of each result plus a couple of high-confidence anchors, in keeping with the
book's "assert the capability" rule.
