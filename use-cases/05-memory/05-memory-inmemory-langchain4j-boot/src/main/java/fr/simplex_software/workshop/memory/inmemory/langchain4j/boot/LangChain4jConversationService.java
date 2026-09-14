package fr.simplex_software.workshop.memory.inmemory.langchain4j.boot;

import fr.simplex_software.workshop.memory.api.*;
import org.springframework.stereotype.*;

@Service
public class LangChain4jConversationService implements ConversationService
{
  private final Assistant assistant;

  public LangChain4jConversationService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer chat(Question question)
  {
    return new Answer(assistant.chat(question.conversationId(), question.question()));
  }
}
