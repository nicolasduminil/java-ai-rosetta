package fr.simplex_software.workshop.mcp.server.springai;

import org.springframework.ai.tool.*;
import org.springframework.ai.tool.method.*;
import org.springframework.context.annotation.*;

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
