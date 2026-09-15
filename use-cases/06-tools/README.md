# 06 — Tool-driven generation

The assistant answers a question it cannot answer on its own by **calling a tool**. A single
`POST /ask` takes `{"question": "..."}` and returns `{"answer": "..."}`; behind it the model is given
one tool — a catalog lookup — and decides for itself when to call it and with what argument.

```
POST /ask   {"question": "What is the shelf reference for item A-100? Answer with the reference only."}
200 OK      {"answer": "The shelf reference for item A-100 is QUARTZ-7."}
```

The shelf references (`A-100` → `QUARTZ-7`, `B-200` → `GRANITE-3`) are arbitrary and unguessable, held
in the shared [`Catalog`](06-tools-common/src/main/java/fr/simplex_software/workshop/tools/catalog/Catalog.java).
A correct answer is therefore proof that the tool was actually invoked — the model could not have
guessed it. Asking about two different items and getting two different references proves the model also
**routes the argument** it extracts from the question, not a constant.

## The Rosetta comparison

Every stack does the same three things — mark a method as a tool, register it, let the framework run the
call-the-tool-then-answer loop — and spells each differently.

| | Spring AI | LangChain4j (Boot) | LangChain4j (Quarkus) |
|---|---|---|---|
| Tool marker | `@Tool` + `@ToolParam` (`org.springframework.ai.tool.annotation`) | `@Tool` + `@P` (`dev.langchain4j.agent.tool`) | same LangChain4j `@Tool` + `@P` |
| Registration | `ChatClient.Builder.defaultTools(bean)` | `AiServices.builder(...).tools(bean)` | `@RegisterAiService(tools = CatalogTool.class)` — declarative |
| Tool object | `@Component` bean | plain Spring bean | `@ApplicationScoped` CDI bean |

## Two findings worth carrying

- **Method parameter names must survive compilation.** A tool's JSON-schema parameter names come from the
  reflected method parameter names; without `-parameters` on `javac` they compile to `arg0`, `arg1`, …,
  the schema is malformed, and the model's call cannot be executed (it leaks back as text). The Quarkus
  parent already set this flag for its own name-based binding; the two Spring Boot stack parents now set it
  too (`maven.compiler.parameters=true`).
- **The model has to be good enough to emit structured tool calls.** `qwen2.5:0.5b`, used everywhere else
  in this book, does not: given a realistic prompt it emits the tool call as plain-text content instead of
  the structured `tool_calls` the frameworks execute. This use case runs on **`qwen2.5:3b`** (the same
  model 03-eval uses as its judge), which emits structured calls reliably. A tool call is two sequential
  inferences — decide, then answer — so the Quarkus client's default 10s timeout is raised for the larger
  model.

## Running it

Each module boots on its own; the shared conformance test
([`AbstractToolCallingIT`](06-tools-common/src/test/java/fr/simplex_software/workshop/tools/conformance/AbstractToolCallingIT.java))
runs against a Testcontainers Ollama that pulls `qwen2.5:3b`.

```
mvn -pl use-cases/06-tools/06-tools-spring-ai-boot      verify
mvn -pl use-cases/06-tools/06-tools-langchain4j-boot    verify
mvn -pl use-cases/06-tools/06-tools-langchain4j-quarkus verify
```
