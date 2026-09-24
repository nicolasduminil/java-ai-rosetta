# 11 — Generative patterns (typed text transformations)

Four everyday generative tasks, each expressed as a prompt plus a Java return type, implemented on all three
stacks and held to one conformance test:

| Task | Endpoint | Returns |
|---|---|---|
| Summarize | `POST /summarize` | `Summary(headline, keyPoints)` |
| Translate | `POST /translate` | `Translation(detectedSourceLanguage, translatedText)` |
| Analyze sentiment | `POST /sentiment` | `SentimentResponse(sentiment)` where `sentiment ∈ {POSITIVE, NEGATIVE, NEUTRAL}` |
| Extract | `POST /extract` | `Extraction(people, organizations, emails)` |

The request body is `{"text": "..."}` for summarize / sentiment / extract, and
`{"text": "...", "targetLanguage": "French"}` for translate. Everything runs on a local Ollama model
(`qwen2.5:3b`, `temperature=0`), with no key and no vector store.

## The Rosetta comparison

Every task is "a prompt and a return type", but the three stacks reach that typed output differently.

| | Spring AI | LangChain4j on Boot | LangChain4j on Quarkus |
|---|---|---|---|
| Style | imperative `ChatClient…call().entity(Type)` | one declarative `@AiService` interface | one declarative `@RegisterAiService` interface |
| Records | `.entity(Summary.class)` | method returns `Summary` | method returns `Summary` |
| Sentiment enum | **wrapped in a record** | **bare `Sentiment` enum** return | **wrapped in a record** |
| Structured output | JSON schema appended to the prompt | format instructions appended to the prompt | Ollama JSON mode (`format=json`) |
| Template braces | `{var}` | `{{var}}` | `{var}` |

### Findings

- **Typed output is prompt-based, not one provider mode.** Structured output works on plain Ollama because
  the format is described to the model rather than enforced by a special API. Spring AI appends a JSON
  schema to the prompt; raw LangChain4j on Boot appends a field-by-field skeleton for records and the list
  of allowed labels for an enum. Quarkus reaches the same reliability by turning on Ollama's JSON mode
  (`quarkus.langchain4j.ollama.chat-model.format=json`), which the records need on a small model.
- **Classification is the sharp asymmetry, and it splits along the JSON boundary.** A top-level enum is only
  first-class on RAW LangChain4j on Boot: the method returns `Sentiment`, and the `EnumOutputParser` appends
  the labels and parses the reply back (case-insensitive, bracket-tolerant). The moment structured output
  goes through a JSON object, a bare enum stops working: Spring AI's `.entity(Sentiment.class)` needs a
  quoted JSON string a small model rarely emits, and Quarkus's JSON mode forces an object rather than a bare
  token. So both JSON-path stacks wrap the enum in a one-field record (`SentimentResponse`), which makes the
  model emit `{"sentiment":"POSITIVE"}` and parses reliably. Same HTTP contract everywhere; only the Boot
  stack returns the enum bare.
- **Reliability, not availability, is the risk.** On a small local model, structured parsing can fail when
  the model adds stray prose; `temperature=0`, shallow records and (on Quarkus) JSON mode keep it stable,
  and a parse failure is mapped to HTTP 422 rather than a 500.

## Running

```bash
cd use-cases/11-generative
# make sure Ollama is running locally (the apps default to http://localhost:11434), then:
ollama pull qwen2.5:3b
# then run any stack (each pinned to a fixed port), in its own terminal:
mvn -pl 11-generative-spring-ai-boot spring-boot:run        # :8080
mvn -pl 11-generative-langchain4j-boot spring-boot:run      # :8081
mvn -pl 11-generative-langchain4j-quarkus quarkus:dev       # :8082
```

Example calls:

```bash
curl -s localhost:8080/sentiment -H 'Content-Type: application/json' \
  -d '{"text":"I absolutely love this, it works perfectly."}'
# -> {"sentiment":"POSITIVE"}

curl -s localhost:8080/extract -H 'Content-Type: application/json' \
  -d '{"text":"John Doe from Acme can be reached at john.doe@acme.com."}'
# -> {"people":["John Doe"],"organizations":["Acme"],"emails":["john.doe@acme.com"]}
```

## Testing

`mvn verify` runs the shared `AbstractGenerativeIT` against an Ollama Testcontainer (`qwen2.5:3b`, no key)
on each stack: summarize returns a non-blank headline and at least one key point; translate detects the
source language and renders the text; clearly positive and negative inputs classify as `POSITIVE` and
`NEGATIVE`; extraction returns the people, organizations and the copied-through email; and blank or malformed
input is rejected with 400. The assertions check the shape of the typed result plus a few high-confidence
content checks, not exact wording, in keeping with the book's "assert the capability" rule.
