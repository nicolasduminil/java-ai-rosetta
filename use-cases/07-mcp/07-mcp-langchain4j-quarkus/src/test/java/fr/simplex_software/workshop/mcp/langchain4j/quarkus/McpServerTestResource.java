package fr.simplex_software.workshop.mcp.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

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
