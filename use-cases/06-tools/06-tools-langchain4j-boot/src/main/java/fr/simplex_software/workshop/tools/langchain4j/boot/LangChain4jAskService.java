package fr.simplex_software.workshop.tools.langchain4j.boot;

import fr.simplex_software.workshop.tools.api.*;
import org.springframework.stereotype.*;

@Service
public class LangChain4jAskService implements AskService
{
  private final Assistant assistant;

  public LangChain4jAskService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer ask(Question question)
  {
    return new Answer(assistant.ask(question.question()));
  }
}
