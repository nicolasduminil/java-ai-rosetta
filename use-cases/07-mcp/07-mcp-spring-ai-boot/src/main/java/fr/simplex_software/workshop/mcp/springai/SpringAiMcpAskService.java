package fr.simplex_software.workshop.mcp.springai;

import fr.simplex_software.workshop.mcp.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.tool.*;
import org.springframework.stereotype.*;

/**
 * The MCP client, the Spring AI way.
 */
@Service
public class SpringAiMcpAskService implements AskService
{
  private final ChatClient chatClient;

  public SpringAiMcpAskService(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools)
  {
    this.chatClient = chatClientBuilder
      .defaultSystem("You are a catalog assistant. Use the available tools to look up catalog items. Answer concisely.")
      .defaultTools(mcpTools)
      .build();
  }

  @Override
  public Answer ask(Question question)
  {
    return new Answer(chatClient.prompt()
      .user(question.question())
      .call()
      .content());
  }
}
