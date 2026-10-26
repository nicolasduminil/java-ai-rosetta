# Use Cases — the module contract

Every use case answers the same question in three (later, possibly four) ways. To keep the
comparison readable, they all follow the identical shape described here. When a new Spring AI
example arrives, it becomes a new numbered directory below and a new `<module>` entry in
[`pom.xml`](pom.xml).

## Directory shape

```
use-cases/
└── NN-<slug>/
    ├── pom.xml                          # aggregator for this use case
    ├── README.md                        # the side-by-side comparison — the actual deliverable
    ├── _original/                       # the Spring AI Gradle sources as provided, untouched
    ├── NN-<slug>-common/                # framework-neutral code ONLY (see boundary below)
    ├── NN-<slug>-spring-ai-boot/        # Spring AI on Spring Boot
    ├── NN-<slug>-langchain4j-boot/      # LangChain4j on Spring Boot
    └── NN-<slug>-langchain4j-quarkus/   # LangChain4j on Quarkus
```

A `NN-<slug>-jakarta-agentic/` sibling slots in without restructuring, once Jakarta Agentic AI
reaches a ratified 1.0 (it is at `1.0.0-M1`, draft, as of August 2026).

The use-case slug is repeated in every module name because Maven rejects a reactor containing two
projects with the same `groupId:artifactId:version` — without the prefix, use case 01 and use case
02 would both contribute a `spring-ai-boot`. Repeating it also keeps the project's "artifactId
equals directory name" rule literally true, and gives the IDE unique module names.

## Aggregation vs. inheritance

These are two different trees and this project uses both:

- **Aggregation** is use-case-first. `NN-<slug>/pom.xml` lists its four modules; a reader finds a
  whole use case — sources, comparison README and untouched original — in one directory.
- **Inheritance** is stack-first. Each module's `<parent>` is a POM in [`../../stacks`](../../stacks),
  not the directory above it:

  | Module | Parent |
  |---|---|
  | `NN-<slug>-common` | `stacks/common-parent` |
  | `NN-<slug>-spring-ai-boot` | `stacks/spring-ai-boot-parent` |
  | `NN-<slug>-langchain4j-boot` | `stacks/langchain4j-boot-parent` |
  | `NN-<slug>-langchain4j-quarkus` | `stacks/langchain4j-quarkus-parent` |

  Reaching outside the directory costs one line:

  ```xml
  <parent>
      <groupId>fr.simplex-software.workshop</groupId>
      <artifactId>spring-ai-boot-parent</artifactId>
      <version>1.0.2</version>
      <relativePath>../../../stacks/spring-ai-boot-parent/pom.xml</relativePath>
  </parent>
  ```

BOMs, shared dependencies and build plugins live in those parents, so a leaf POM declares only its
own module's dependencies. That is not just brevity: it makes the "dependency count" row of the
metrics table a readable diff instead of boilerplate noise.

## The sharing boundary

`NN-<slug>-common/` holds **only** what is genuinely framework-neutral:

- domain records and DTOs
- the REST request/response contract
- the abstract conformance test and its Testcontainers fixtures

It must **never** hold AI wiring, model configuration, prompt construction, or anything that
differs between Spring AI and LangChain4j. Those differences are the entire point of the
project — sharing them would hide exactly what a reader came to see.

This deliberately narrows the project's "maximize code sharing" convention.

## The conformance contract

The `common` module defines one abstract integration test per use case. Each implementation module
extends it and points it at its own running application. Same inputs, same assertions, three
stacks — that is what turns three code samples into a defensible claim of equivalence.

The abstract test lives in the common module's `src/test/java` and ships as a **test-jar**, which
`stacks/common-parent` configures. A stack module therefore declares its common module twice — once
normally for the DTOs, once as `<type>test-jar</type><scope>test</scope>` for the contract — and no
test library ends up on its production classpath.

The suite talks to each application over **HTTP with REST Assured**, never through an in-process
API. That is what lets one test class hold a Spring Boot application and a Quarkus application to
the same standard; a subclass supplies only the base URI of its running application. REST Assured
and Jackson are inherited from the root POM, so the suite never borrows the stack's JSON mapper —
Spring Boot 4 is on Jackson 3 (`tools.jackson`), Quarkus on Jackson 2 (`com.fasterxml`), and REST
Assured only detects the latter.

Assertions must be **semantic, not exact-string**: LLM output is non-deterministic even at
temperature zero. Assert on structure, on required content, on schema conformance — never on
a literal completion.

Tests run against a local **Ollama** container via Testcontainers, so CI needs no API key,
costs nothing, and is reproducible. The image and model are set once in the root `pom.xml`
(`ollama.image`, `ollama.model`).

## The per-use-case README

This is the deliverable a reader actually consumes. It should carry:

1. **What the use case does** — the problem, framed independently of any framework.
2. **The three implementations** side by side, with the interesting fragments quoted inline.
3. **A metrics table** — lines of code, dependency count, startup time, native-image feasibility.
4. **A differences section** — where the frameworks genuinely diverge, including anything that
   has no clean equivalent. Gaps are the most interesting content; do not paper over them.

## Adding a use case

1. Drop the provided Spring AI Gradle sources into `NN-<slug>/_original/`.
2. Write `NN-<slug>-common/` — domain types first, then the abstract conformance test. It comes
   before any implementation: the contract is what the three tracks are held to, not a description
   of whichever one was written first.
3. Convert the original to Maven as `NN-<slug>-spring-ai-boot/`.
4. Implement `NN-<slug>-langchain4j-boot/` and `NN-<slug>-langchain4j-quarkus/` against that same
   contract.
5. Register the use case in [`pom.xml`](pom.xml) and write the comparison README.
6. Bump the PATCH version at the project root.

Nothing in these steps touches `stacks/`. If a use case needs a dependency, it belongs in that
use case's leaf POM; only something genuinely shared by *every* use case on a track belongs in a
stack parent.
