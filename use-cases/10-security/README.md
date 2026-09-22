# 10 — Security (securing AI applications)

A secured, multi-user RAG assistant on local Ollama + Qdrant, reached over a JWT bearer token. Four security
concerns are built across the three stacks and held to one shared conformance test, with a fifth (content
moderation) gated behind an OpenAI key. Back to local-first: no key needed for everything except moderation.

The assistant answers only from documents the caller owns (plus public ones), refuses prompt injection,
never leaks a forbidden codename, guards an admin-only tool, and (with a key) moderates toxic input. Tool
calling needs a capable model, so this use case runs on `qwen2.5:3b` (as use case 06 does), with
`nomic-embed-text` for embeddings.

## Users, roles, and the corpus

The conformance test mints RS256 JWTs (signed with a test-only key) for three users, and the apps verify them
with the public key shipped in the common module:

| User  | Roles (`groups` claim) | Sees documents owned by |
|-------|------------------------|-------------------------|
| alice | USER                   | alice, public           |
| bob   | USER                   | bob, public             |
| admin | USER, ADMIN            | admin, public (+ the admin tool) |

The corpus is three owner-tagged files: `public.txt` (Reykjavik, Lumen, codename BLUEFIN), `alice.txt`
(alice's budget code APRICOT), `bob.txt` (bob's access code GRAPEFRUIT).

## Endpoint

| Endpoint | What it does |
|---|---|
| `POST /ask` | Authenticated (JWT bearer). `{"question":"..."}` -> `{"answer":"..."}`. Retrieval is filtered to the caller's documents; every security violation returns HTTP 403. |

## The Rosetta comparison

| Capability | Spring AI | LangChain4j on Boot | LangChain4j on Quarkus |
|---|---|---|---|
| Authentication | OAuth2 resource server (RS256 `public-key-location`) | OAuth2 resource server (RS256) | MP JWT (`mp.jwt.verify.*`) |
| Roles from token | `groups` -> `ROLE_` via `JwtAuthenticationConverter` | same | `groups` -> roles (MP JWT) |
| Vector-store ACL | `FilterExpressionBuilder` owner filter | `metadataKey("owner")` filter | `metadataKey("owner")` filter |
| Guardrails (injection + leak) | **hand-written `CallAdvisor`** (only `SafeGuardAdvisor` blocklist is built in) | **`@InputGuardrails`/`@OutputGuardrails` SPI** | **`@InputGuardrails`/`@OutputGuardrails` SPI** |
| Tool authorization | `@PreAuthorize` on `@Tool` | imperative check (CGLIB proxy hides `@Tool`) + rethrowing tool-error handler | imperative check (`@RolesAllowed` is bypassed) + qualified rethrowing tool-error handler |
| Content moderation (OpenAI-gated) | Spring AI `OpenAiModerationModel` | LangChain4j `OpenAiModerationModel` | LangChain4j `OpenAiModerationModel` |
| Every violation | HTTP 403 | HTTP 403 | HTTP 403 |

### Findings

- **Authentication is shared by one keypair.** One RS256 keypair: the public key ships in the common module
  and is read by both Spring Security's resource server (`public-key-location`) and Quarkus MP JWT
  (`mp.jwt.verify.publickey.location`); the conformance test mints tokens with the test-only private key
  using Nimbus. The `groups` claim carries the roles for both worlds.
- **Vector-store access control is symmetric.** Tag each document with an `owner` at ingestion, then filter
  retrieval by `owner == <caller> OR owner == public`. The caller comes from the security context (Spring
  `SecurityContextHolder`, Quarkus injected `JsonWebToken`), never the request body.
- **Guardrails are the sharp asymmetry.** LangChain4j has a first-class input/output guardrail SPI shared by
  Boot and Quarkus; Spring AI has only the built-in `SafeGuardAdvisor` (a sensitive-word blocklist), so the
  injection and leak checks are a hand-written `CallAdvisor`.
- **Tool authorization is the deepest one, and declarative method security silently fails on both LangChain4j
  stacks.** On Boot a `@PreAuthorize` on a `@Tool` makes Spring wrap the bean in a CGLIB proxy whose methods
  no longer carry `@Tool` (the tool is not found); on Quarkus a `@RolesAllowed` on the `@Tool` is simply not
  enforced (the tool executor bypasses the CDI security interceptor). On both, the role is checked
  imperatively inside the tool. And every stack swallows a tool exception by default (feeds it back to the
  model, so a denied call still answers 200), so each needs a different switch to make a denial a 403: Spring
  AI a `DefaultToolExecutionExceptionProcessor` with `alwaysThrow(true)`; LangChain4j-Boot a rethrowing
  `toolExecutionErrorHandler`; LangChain4j-Quarkus a rethrowing `ToolExecutionErrorHandler` bean carrying the
  extension's `@DefaultToolExecutionErrorHandler` qualifier. Spring AI, by contrast, tolerates a
  `@PreAuthorize` `@Tool`.
- **Moderation forces a cloud call.** No stack ships a local moderation model, so moderation uses OpenAI
  (Spring AI's `OpenAiModerationModel`, LangChain4j's on the other two). It is active only when
  `OPENAI_API_KEY` is set; without a key the model is absent and moderation is skipped, so the rest of the
  use case stays local and offline.

## Running

```bash
cd use-cases/10-security
docker compose up -d                       # qdrant (add --profile with-ollama to run Ollama too)
ollama pull qwen2.5:3b && ollama pull nomic-embed-text
# then run each app (each pinned to a fixed port), in its own terminal:
mvn -pl 10-security-spring-ai-boot spring-boot:run        # :8080
mvn -pl 10-security-langchain4j-boot spring-boot:run      # :8081
mvn -pl 10-security-langchain4j-quarkus quarkus:dev       # :8082
```

Every call needs a JWT bearer token, so a manual `curl` needs one minted with the demo key (or from your own
identity provider); the conformance test is the simplest way to exercise the endpoints.

## Testing

`mvn verify` runs the shared `AbstractSecurityIT` against Ollama + Qdrant Testcontainers on each stack
(`qwen2.5:3b`, no key): a request without a token is 401; a user retrieves their own and public documents but
never another user's; prompt injection and a codename leak are refused with 403; the admin-only tool is
denied to a non-admin (403) and allowed to an admin (200); a blank or malformed body is 400. The moderation
test is gated behind `OPENAI_API_KEY` and is skipped without it (run `OPENAI_API_KEY=... mvn verify` to
exercise it). The JWT keys under the common module are demo/test-only and must never be used in production.
