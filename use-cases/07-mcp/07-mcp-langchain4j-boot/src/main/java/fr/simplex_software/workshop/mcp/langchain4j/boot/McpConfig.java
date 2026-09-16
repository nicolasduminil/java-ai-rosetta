package fr.simplex_software.workshop.mcp.langchain4j.boot;

import dev.langchain4j.mcp.*;
import dev.langchain4j.mcp.client.*;
import dev.langchain4j.mcp.client.transport.*;
import dev.langchain4j.mcp.client.transport.http.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Wires the MCP client the LangChain4j way. A {@link StreamableHttpMcpTransport} connects to the server's
 * {@code /mcp} endpoint; a {@link DefaultMcpClient} speaks the protocol over it; and an {@link McpToolProvider}
 * turns the server's tools into a {@link dev.langchain4j.service.tool.ToolProvider} that the AI service consults.
 * The {@link ChatModel} is auto-configured by the Spring Boot 4 Ollama starter.
 */
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
