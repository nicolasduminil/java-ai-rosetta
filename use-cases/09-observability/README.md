# 09 — Observability (observing AI operations)

A small RAG workload (chat grounded in a seeded Qdrant vector store, on local Ollama) instrumented so its
model calls can be observed: actuator/management metrics, token counts, vector-store inspection, Prometheus
scraping and a Grafana dashboard. Back to the local-first premise (08 was the OpenAI exception): no key,
offline, free.

## Endpoints

| Endpoint | What it does |
|---|---|
| `POST /ask` | Retrieve-then-answer over the corpus (drives a chat + embedding + vector call) |
| `GET /vectorstore/count` | `{"count": N}` — points (embedded chunks) in the collection |
| `GET /vectorstore/search?query=...` | The top matching chunks (Spring AI stack) |
| `GET /actuator/prometheus` (Boot) / `GET /q/metrics` (Quarkus) | The Prometheus scrape endpoint |

## The Rosetta comparison

| | Spring AI 2.0.0 | LangChain4j 1.17.2 on Boot | Quarkus-LangChain4j 1.12.2 |
|---|---|---|---|
| Model-call duration metric | built-in `gen_ai.client.operation` | **hand-wired** `ChatModelListener` | built-in `gen_ai.client.operation.duration` |
| Token metric | built-in `gen_ai.client.token.usage` | **hand-wired** to the same name | built-in `gen_ai.client.token.usage` |
| Vector-store metric | built-in `db.vector.client.operation` | not built-in | not built-in |
| How enabled | add actuator + Prometheus registry, no code | write the listener bean | add `quarkus-micrometer-registry-prometheus`, no code |
| Scrape endpoint | `/actuator/prometheus` | `/actuator/prometheus` | `/q/metrics` |

### Findings

- **Spring AI and Quarkus-LangChain4j emit `gen_ai.*` metrics out of the box** (OpenTelemetry GenAI
  semantic conventions), turned on purely by adding the metrics dependency. Spring AI auto-records model,
  embedding and vector-store observations once actuator supplies an `ObservationRegistry` and the Prometheus
  registry a `MeterRegistry`. Quarkus-LangChain4j auto-registers a `MetricsChatModelListener` the moment
  `quarkus-micrometer` is on the classpath.
- **LangChain4j on Boot has nothing built in.** Its core ships no Micrometer or OpenTelemetry module, so the
  `TokenUsageChatModelListener` here is hand-written: it reads `chatResponse().tokenUsage()` and records a
  `gen_ai.client.token.usage` counter plus a `gen_ai.client.operation` timer into the `MeterRegistry`. It
  deliberately uses the same metric names the other two emit, so the same dashboard and the same test work
  across all three. The striking part is that the *same* LangChain4j library is auto-instrumented by the
  Quarkus extension but bare on Boot: the wrapper decides, not the library.
- **Token counts** come off the response (`getPromptTokens()`/`getCompletionTokens()` in Spring AI,
  `tokenUsage().inputTokenCount()/outputTokenCount()` in LangChain4j) and, once a registry is present, also
  appear as the `gen_ai_client_token_usage_total{gen_ai_token_type="input|output|total"}` counter.
- **Vector-store inspection** has no actuator endpoint in any stack, so `/vectorstore/count` reads the point
  count straight from the Qdrant client. Spring AI additionally gets a `db.vector.client.operation` timer for
  free.

## Running

Backing services and the observability stack:

```bash
cd use-cases/09-observability
# If you already run Ollama natively on localhost:11434 (the apps' default):
docker compose up -d                       # qdrant, prometheus, grafana (ollama is skipped)
ollama pull qwen2.5:0.5b
ollama pull nomic-embed-text
# If you do NOT run Ollama locally, start it in Compose too (adds it on 11434):
#   docker compose --profile with-ollama up -d
#   docker compose exec ollama ollama pull qwen2.5:0.5b
#   docker compose exec ollama ollama pull nomic-embed-text
```

Run the three apps (each is pinned to a fixed port in its `application.properties`, matching
`prometheus.yml`, so no port flags are needed). Run each in its own terminal:

```bash
mvn -pl use-cases/09-observability/09-observability-spring-ai-boot spring-boot:run          # :8080
mvn -pl use-cases/09-observability/09-observability-langchain4j-boot spring-boot:run        # :8081
mvn -pl use-cases/09-observability/09-observability-langchain4j-quarkus quarkus:dev         # :8082
```

Make a few calls, then open Grafana at http://localhost:3000 (anonymous admin) and the pre-provisioned
"GenAI operations" dashboard, or Prometheus at http://localhost:9090.

```bash
curl -s localhost:8080/ask -H 'Content-Type: application/json' \
  -d '{"question":"Where is Aurora Labs based?"}'
curl -s localhost:8080/vectorstore/count
curl -s localhost:8080/actuator/prometheus | grep gen_ai_client_token_usage
```

## Testing

`mvn verify` runs the shared `AbstractObservabilityIT` against Ollama + Qdrant Testcontainers on every
stack (no key needed): it answers a corpus fact, asserts the vector store is populated, and asserts that
`gen_ai_client_token_usage` appears at the stack's Prometheus endpoint after a model call. The Prometheus
and Grafana artifacts are shown by construction and documented here, not gated by the tests.
