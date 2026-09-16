# 07 — Model Context Protocol (MCP)

Chapter 6's tool was a *local* `@Tool` method. Here the same catalog tool is exposed by a **separate MCP
server**, and each stack becomes an **MCP client** that discovers the remote tool and lets the model call it —
same `POST /ask`, but the tool now lives in another process reached over a transport.

```
POST /ask   {"question": "What is the shelf reference for item A-100? Answer with the reference only."}
200 OK      {"answer": "The shelf reference for item A-100 is QUARTZ-7."}
```

The shelf references are arbitrary (`A-100` → `QUARTZ-7`, `B-200` → `GRANITE-3`), so a correct answer proves the
client discovered and called the *remote* tool — not that the model guessed.

## Modules

- **`07-mcp-server-spring-ai`** — a Spring AI MCP **server** exposing the catalog `@Tool` as an MCP tool
  (`MethodToolCallbackProvider` → `ToolCallbackProvider` bean), over **Streamable HTTP** (`/mcp`).
- **`07-mcp-spring-ai-boot`**, **`07-mcp-langchain4j-boot`**, **`07-mcp-langchain4j-quarkus`** — three MCP
  **clients**, each an `/ask` app that connects to the server and hands its tools to `qwen2.5:3b`.

The client conformance test (`AbstractMcpToolCallingIT`) is transport-agnostic: it just checks `/ask` returns
the tool's unguessable result for two item codes (invocation + argument routing). A shared `McpServerProcess`
helper launches the server `-exec.jar` as a subprocess so the test works uniformly on Boot and Quarkus.

## The Rosetta comparison — how each client wires MCP

| | Register the MCP client | Attach its tools to the model |
|---|---|---|
| Spring AI | `spring.ai.mcp.client.streamable-http.connections.*` | a `ToolCallbackProvider` bean → `ChatClient.defaultToolCallbacks(...)` |
| LangChain4j (Boot) | `StreamableHttpMcpTransport` + `DefaultMcpClient` | `McpToolProvider` → `AiServices.toolProvider(...)` |
| LangChain4j (Quarkus) | `quarkus.langchain4j.mcp.<name>.*` (declarative) | **`@McpToolBox`** on the AI-service method |

Note the Quarkus opt-in: a bare `@RegisterAiService` does **not** auto-attach MCP tools — `@McpToolBox` is
required.

## Transports

MCP defines several transports; this is a sub-choice of "how the client reaches the server":

- **Streamable HTTP** — the current remote transport (MCP spec 2025-03-26+), a single `/mcp` endpoint. **Used
  here.**
- **HTTP+SSE** — its **deprecated** predecessor (two endpoints, `/sse` + `/mcp/message`); still available for
  older peers.
- **stdio** — the local transport: the client launches the server as a subprocess and speaks JSON-RPC over its
  stdin/stdout. Ideal for a co-located tool server; every stack supports it (Spring AI
  `spring.ai.mcp.client.stdio.*`, LangChain4j `StdioMcpTransport`, Quarkus `transport-type=stdio` + `command`).
  Not exercised here (our server is a remote HTTP service), but it is the same MCP once connected.

### Stateful vs stateless Streamable HTTP, and a real interop matrix

Streamable HTTP has two server modes. **Stateful** keeps a session and may push to the client (notifications,
sampling) and streams responses over SSE. **Stateless** is session-less: each request gets a plain-JSON
response — the right fit for a stateless tool server, and horizontally scalable. Our server runs **stateless**.

That choice is also forced by interop. Verified against the pinned versions:

| Client | Stateful streamable | Stateless streamable |
|---|---|---|
| Spring AI | ✅ | ✅ |
| LangChain4j (Boot) | ✅ | ✅ |
| LangChain4j (Quarkus) | ❌ | ✅ |

The **Quarkus** MCP client (`quarkus-langchain4j 1.12.2`) **hangs** against a *stateful* streamable server: its
SSE parser does not accept the spec-valid no-space field form (`data:{json}`) that real servers — Spring AI's
stateful server included — emit. Adding a space (`data: {json}`) makes it work, and LangChain4j's own
transport handles both. A minimal standalone reproducer for that bug lives at the repo root
(`mcp-quarkus-streamable-repro/`, gitignored) and is being filed upstream. Stateless sidesteps it (plain-JSON
responses) and is uniform across all three clients.

## Running it

Each client boots on its own; its test launches the MCP server subprocess and runs the shared conformance test
against a Testcontainers Ollama that pulls `qwen2.5:3b` (tool calling needs it — see Chapter 7).

```
mvn -pl use-cases/07-mcp/07-mcp-spring-ai-boot      verify
mvn -pl use-cases/07-mcp/07-mcp-langchain4j-boot    verify
mvn -pl use-cases/07-mcp/07-mcp-langchain4j-quarkus verify
```
