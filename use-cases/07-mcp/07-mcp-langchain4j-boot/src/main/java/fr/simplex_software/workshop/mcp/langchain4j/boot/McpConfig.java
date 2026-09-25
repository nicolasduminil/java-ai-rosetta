package fr.simplex_software.workshop.mcp.langchain4j.boot;

import dev.langchain4j.mcp.*;
import dev.langchain4j.mcp.client.*;
import dev.langchain4j.mcp.client.transport.*;
import dev.langchain4j.mcp.client.transport.http.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class McpConfig
{
  @Bean(destroyMethod = "close")
  McpClient catalogMcpClient(@Value("${mcp.server.url}") String serverUrl)
  {
    McpTransport transport = new StreamableHttpMcpTransport.Builder()
      .url(serverUrl + "/mcp")
      .build();
    return DefaultMcpClient.builder()
      .transport(transport)
      .clientName("catalog-client")
      .build();
  }

  @Bean
  Assistant assistant(ChatModel chatModel, McpClient catalogMcpClient)
  {
    var toolProvider = McpToolProvider.builder()
      .mcpClients(catalogMcpClient)
      .build();
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .toolProvider(toolProvider)
      .build();
  }
}
