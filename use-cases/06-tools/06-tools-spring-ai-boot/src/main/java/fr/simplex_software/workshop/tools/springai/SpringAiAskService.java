package fr.simplex_software.workshop.tools.springai;

import fr.simplex_software.workshop.tools.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiAskService implements AskService
{
  private final ChatClient chatClient;

  public SpringAiAskService(ChatClient.Builder chatClientBuilder, CatalogTool catalogTool)
  {
    this.chatClient = chatClientBuilder
      .defaultSystem("You are a catalog assistant. Use the available tools to look up catalog items. Answer concisely.")
      .defaultTools(catalogTool)
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
