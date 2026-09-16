package fr.simplex_software.workshop.mcp.springai;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.mcp.conformance.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiMcpToolCallingIT extends AbstractMcpToolCallingIT
{
  static final McpServerProcess MCP_SERVER = McpServerProcess.start();

  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.ai.ollama.base-url", OllamaTestSupport::baseUrl);
    registry.add("spring.ai.ollama.chat.model", OllamaTestSupport::model);
    registry.add("spring.ai.mcp.client.streamable-http.connections.catalog.url", MCP_SERVER::baseUrl);
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
