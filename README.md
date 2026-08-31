# Java AI Rosetta

One AI use case. Three implementations. The same tests.

This is a didactic project comparing **Spring AI** and **LangChain4j** across **Spring Boot**
and **Quarkus**. Each use case starts from a Spring AI example and is re-implemented, without
changing what it does, on every stack — then held to a single shared conformance test suite so
the claim "these are equivalent" is proven rather than asserted.

It is a Rosetta Stone, not a shootout. The interesting output is not a winner; it is the
diff.

## The stacks

| Track | Framework | Runtime |
|---|---|---|
| `spring-ai-boot` | Spring AI | Spring Boot |
| `langchain4j-boot` | LangChain4j | Spring Boot |
| `langchain4j-quarkus` | LangChain4j (Quarkiverse extension) | Quarkus |
| `jakarta-agentic` | Jakarta Agentic AI | *deferred — see below* |

## Version matrix

All versions verified against Maven Central on 2026-08-10 and declared once in the root
[`pom.xml`](pom.xml).

| Component | Version | Note |
|---|---|---|
| Java | 21 | LTS; above the Boot 4.1 floor of 17, comfortable for Quarkus native |
| Spring Boot | 4.1.0 | |
| Spring AI | 2.0.0 | Compiled against Boot 4.1.0 — no version lag |
| Quarkus | 3.38.1 | |
| Quarkus LangChain4j | 1.12.2 | Built against Quarkus 3.33.2 — see caveat |
| LangChain4j | 1.17.2 | Deliberately **not** the newest — see caveat |
| Testcontainers | 2.0.5 | 2.x renamed modules to `testcontainers-<name>` |
| JUnit | 6.1.3 | floor only — see below |
| Jackson | 2.21.4 | conformance suite only — see below |

**JUnit is 6.1.3 in the root POM but modules resolve Boot's 6.0.3.** A stack parent's BOM import
sits closer to the leaf than the root's `junit-bom`, so it wins. That is the correct outcome —
each stack gets the JUnit version its framework was tested against — but it does mean the root is
the single source of versions only for what no stack BOM already manages.

**Two Jackson majors coexist, deliberately.** Spring Boot 4 ships Jackson 3
(`tools.jackson.core:jackson-databind`); Quarkus is on Jackson 2 (`com.fasterxml`). The conformance
suite talks HTTP and parses JSON itself, so it carries its own Jackson 2 at test scope rather than
depending on whichever the stack under test happens to use. The package roots differ, so nothing
collides.

### Two deliberate version choices

**LangChain4j is pinned to 1.17.2, not the newest 1.18.1.** That is the version the Quarkus
LangChain4j extension ships. Pinning both LangChain4j tracks to the same core version means
the only variable between them is the runtime — which is the entire point of running them side
by side. Bump `langchain4j.version` and `quarkus-langchain4j.version` together, never apart.

**Quarkus is on 3.38.1 while the extension was built against 3.33.2.** Quarkiverse extensions
normally work across 3.x minors, so this takes the newer runtime. `mvn dependency:resolve` across
both BOMs is clean, so the documented fallback — setting `quarkus.version` to `3.33.2` — has not
been needed. It stays recorded here because the version dance between Quarkus and its extensions
is itself worth teaching, and because a later bump may resurrect it.

## Repository layout

```
.
├── .circleci/config.yml   # machine executor + Ollama warm-up (Testcontainers needs a real daemon)
├── pom.xml                # root parent + aggregator; the single source of every version
├── stacks/                # parent POMs only — no code, no use cases
│   ├── common-parent/
│   ├── spring-ai-boot-parent/
│   ├── langchain4j-boot-parent/
│   └── langchain4j-quarkus-parent/
├── test-support/          # shared conformance infrastructure (the single Ollama Testcontainers helper)
├── test-support-quarkus/  # the shared Quarkus test resource wiring that container into Quarkus config
├── test-support-langchain4j/ # the shared hand-rolled LangChain4j evaluation judge (use case 03)
└── use-cases/
    ├── README.md          # the module contract every use case follows
    ├── 01-simple/         # one endpoint: forward a question, return text
    ├── 02-templates/      # templated prompt + injected context + structured output
    ├── 03-eval/           # LLM-as-judge evaluation (relevancy + factual accuracy) — four modules, each
    │                      #   booting its 02 application unchanged and scoring its answers
    └── 04-rag-classic/    # RAG over ingested PDFs, imperative retrieval — a Camel/Tika ingestion
                           #   pipeline feeding a stack-native embed/store/retrieve into Qdrant
```

**Aggregation is use-case-first; inheritance is stack-first.** A use case directory holds its four
modules, its comparison README and the untouched original — so the thing a reader consumes lives in
one place. But each module's `<parent>` points into `stacks/`, so BOMs, shared dependencies and
build plugins are declared once per track rather than once per track *per use case*. Maven treats
`<modules>` and `<parent>` as independent trees; this project uses that.

Two things follow from it. The Spring Boot and Quarkus BOMs both manage Jackson, Netty, SLF4J and
JUnit — importing both into one `dependencyManagement` would let the first-declared silently win
for every overlap, so they must live in parents that never meet. And because the boilerplate is
hoisted out, a leaf POM lists only what that use case genuinely needs, which is what makes the
dependency-count comparison meaningful.

See [`use-cases/README.md`](use-cases/README.md) for the shape each use case must take and how
to add one.

## Testing approach

Tests run against a local **Ollama** model in a Testcontainers container. No API keys, no
per-run cost, no rate limits, reproducible in CI — and supported identically by all three
stacks, which keeps the comparison honest.

Each use case defines **one abstract conformance test** in its `common/` module; every
implementation extends it. Assertions are semantic, never exact-string — LLM output is
non-deterministic even at temperature zero. The Testcontainers Ollama helper that stands up the
shared model lives once in `test-support/` — the conformance *scenarios* stay per use case, since
they encode what each use case must do, but the container plumbing is identical everywhere and is
declared in a single place.

```shell
mvn verify          # builds every module and runs the conformance suite
```

Requires Java 21+, Maven 3.9+, and a running Docker daemon.

## On Jakarta Agentic AI

The fourth track is deliberately deferred. As of August 2026 Jakarta Agentic AI is at
`1.0.0-M1` — a draft specification published for community feedback, not ratified, and not
initially seeking inclusion in the Jakarta EE Platform or any profile. Adopting it now would
mean rewriting every use case as the API moves. The layout leaves room for it; it gets added
at 1.0 final.

## Conventions

Project-wide rules are in [`CLAUDE.md`](CLAUDE.md): group ID `fr.simplex-software.workshop`,
Maven multi-module with a parent POM, Testcontainers integration tests over mocks, no Lombok,
semantic versioning with a PATCH bump per generated version.
