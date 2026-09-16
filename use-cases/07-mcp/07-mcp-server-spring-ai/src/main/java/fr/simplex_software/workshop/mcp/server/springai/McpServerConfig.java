package fr.simplex_software.workshop.mcp.server.springai;

import org.springframework.ai.tool.*;
import org.springframework.ai.tool.method.*;
import org.springframework.context.annotation.*;

/**
 * Publishes the {@link CatalogTool} as an MCP tool. The Spring AI MCP server auto-configuration discovers
 * every {@link ToolCallbackProvider} bean and exposes its callbacks over the configured transport, so a
 * single {@code MethodToolCallbackProvider} built from our {@code @Tool} bean is all the wiring needed.
 */
@Configuration
public class McpServerConfig
{
  @Bean
  ToolCallbackProvider catalogTools(CatalogTool catalogTool)
  {
    return MethodToolCallbackProvider.builder()
      .toolObjects(catalogTool)
      .build();
  }
}
