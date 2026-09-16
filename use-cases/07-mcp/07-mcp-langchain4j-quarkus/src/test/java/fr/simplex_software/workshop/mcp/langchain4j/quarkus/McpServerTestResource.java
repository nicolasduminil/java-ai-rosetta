package fr.simplex_software.workshop.mcp.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

/**
 * Starts the MCP server (as a subprocess) before the Quarkus app and points the MCP client's runtime URL at
 * it. The server runs on a random port, so its {@code /mcp} endpoint is only known once it is up.
 */
public class McpServerTestResource implements QuarkusTestResourceLifecycleManager
{
  private McpServerProcess server;

  @Override
  public Map<String, String> start()
  {
    server = McpServerProcess.start();
    return Map.of("quarkus.langchain4j.mcp.catalog.url", server.baseUrl() + "/mcp");
  }

  @Override
  public void stop()
  {
    if (server != null)
      server.close();
  }
}
