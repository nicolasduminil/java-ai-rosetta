package fr.simplex_software.workshop.mcp.langchain4j.boot;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.mcp.conformance.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LangChain4jMcpToolCallingIT extends AbstractMcpToolCallingIT
{
  static final McpServerProcess MCP_SERVER = McpServerProcess.start();

  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry)
  {
    registry.add("langchain4j.ollama.chat-model.base-url", OllamaTestSupport::baseUrl);
    registry.add("langchain4j.ollama.chat-model.model-name", OllamaTestSupport::model);
    registry.add("mcp.server.url", MCP_SERVER::baseUrl);
  }

  @AfterAll
  static void stopServer()
  {
    MCP_SERVER.close();
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
