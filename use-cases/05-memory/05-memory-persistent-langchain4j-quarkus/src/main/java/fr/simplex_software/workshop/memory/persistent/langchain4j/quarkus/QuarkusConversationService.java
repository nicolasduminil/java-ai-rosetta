package fr.simplex_software.workshop.memory.persistent.langchain4j.quarkus;

import fr.simplex_software.workshop.memory.api.*;
import jakarta.enterprise.context.*;

@ApplicationScoped
public class QuarkusConversationService implements ConversationService
{
  private final Assistant assistant;

  public QuarkusConversationService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer chat(Question question)
  {
    return new Answer(assistant.chat(question.conversationId(), question.question()));
  }
}
