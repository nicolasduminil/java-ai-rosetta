# Claude Code Template for Spring Boot / Quarkus / Camel Applications

A lean, opinionated starting point for enterprise Java applications developed with Claude Code.
The targeted stack spans **Spring Boot 3.x**, **Quarkus 3.x**, **Apache Camel**, **Docker /
Docker Compose**, **Kubernetes**, and **CircleCI**, built with **Maven** (parent POM + modules).

This repository carries only the **project-specific** pieces — the conventions in
[CLAUDE.md](CLAUDE.md) and the parent [pom.xml](pom.xml). The reusable **agents** and **skills**
are installed once at **user scope** (`~/.claude/`) so they apply to *every* project on your
machine, with a single source of truth and no per-project drift.

> 💡 Use this repo as a **GitHub Template** ("Use this template") to bootstrap new apps with a
> clean history, the parent POM, and the conventions already in place.

## What's in this repo

```shell
.
├── .claude
│   └── settings.local.json   # project-scoped tool permissions (mvn, docker, etc.)
├── CLAUDE.md                 # project conventions Claude must follow
├── README.md
├── pom.xml                   # parent POM (group id: fr.simplex-software.workshop)
└── tasks
    └── lessons.md            # self-improvement log (see CLAUDE.md)
```

Generated artifacts (modules, `.circleci/config.yml`, `docker-compose.yml`, Dockerfiles,
Kubernetes manifests) are created per application as you build it.

## Conventions

Project-wide rules Claude follows are defined in [CLAUDE.md](CLAUDE.md). Highlights:

- Group ID / base package: `fr.simplex-software.workshop`
- Maven multi-module layout with a parent POM; shared code in a common module
- Integration tests with Testcontainers preferred over mock-based unit tests
- CircleCI pipeline generated under `.circleci/`
- Docker Compose for all runtime components
- Semantic versioning (bump PATCH per generated version); no Lombok

## Reusable capabilities (installed at user scope)

These live in `~/.claude/` — **not** in this repo — and are available in all your projects.
Claude loads them automatically by context, or you can invoke a skill explicitly with
`/skill-name`.

### Agents — `~/.claude/agents/`

| Agent | Focus |
|-------|-------|
| `java-architect` | Enterprise Java architecture, Spring Boot/Quarkus/Camel design |
| `spring-boot-engineer` | Spring Boot 3+ microservices and reactive applications |
| `quarkus-engineer` | Quarkus 3.37+ cloud-native and reactive applications |
| `docker-expert` | Container image build, optimization, and hardening |
| `kubernetes-specialist` | Cluster design, workloads, and troubleshooting |
| `devops-engineer` | CI/CD pipelines, automation, deployment workflows |
| `security-engineer` | Threat modeling, DevSecOps, vulnerability management |
| `test-automator` | Test framework design and CI/CD test integration |
| `code-reviewer` | Comprehensive code, security, and quality reviews |

### Skills — `~/.claude/skills/`

| Skill | Description |
|-------|-------------|
| `java-architect` | WebFlux, JPA optimization, Spring Security (with references) |
| `spring-boot-engineer` | Spring Boot 3.x configs, controllers, security, data (with references) |
| `spring-boot-patterns` | Spring Boot best practices and architecture |
| `quarkus-engineer` | Quarkus 3.x generator: JAX-RS, Panache, Security, SmallRye (with references) |
| `quarkus-patterns` | Quarkus 3.x: REST, Panache, CDI, native builds, testing |
| `camel-patterns` | Apache Camel + Spring Boot: routes, EIPs, error handling |
| `jpa-patterns` | JPA/Hibernate pitfalls: N+1, lazy loading, transactions |
| `logging-patterns` | Structured logging, SLF4J, MDC, AI-friendly formats |
| `docker-patterns` | Multi-stage Dockerfiles, layered jars, hardening, Compose |
| `kubernetes-patterns` | Deployments, probes, ConfigMaps/Secrets, HPA, Kustomize |
| `circleci-patterns` | CircleCI 2.1 for Maven, caching, Testcontainers in CI |
| `design-patterns` | GoF patterns with modern Java examples |
| `clean-code` | DRY, KISS, YAGNI, naming, refactoring |
| `java-code-review` | Systematic Java review checklist |
| `api-contract-review` | REST API semantics, versioning, compatibility |

## First-time machine setup

The agents and skills are maintained at user scope. On a new machine, install them into
`~/.claude/` from wherever you keep them under version control (e.g. a dotfiles / `claude-config`
repo):

```shell
mkdir -p ~/.claude/agents ~/.claude/skills
cp -r <your-claude-config>/agents/.  ~/.claude/agents/
cp -r <your-claude-config>/skills/.  ~/.claude/skills/
```

Once present, every project — including any app generated from this template — picks them up
automatically.

## Usage

1. On GitHub, click **"Use this template"** to create a new repository (or clone and reset
   history locally).
2. Rename the project and set the `pom.xml` `artifactId` to match the new directory name.
3. Start Claude Code in the project directory and describe the application you want to build:

   ```shell
   cd my-new-app
   claude
   ```

Claude applies the conventions in `CLAUDE.md` and draws on the user-scope agents and skills
automatically.
