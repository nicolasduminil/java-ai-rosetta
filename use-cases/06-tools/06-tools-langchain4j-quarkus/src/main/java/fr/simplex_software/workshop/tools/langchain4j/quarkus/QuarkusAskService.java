package fr.simplex_software.workshop.tools.langchain4j.quarkus;

import fr.simplex_software.workshop.tools.api.*;
import jakarta.enterprise.context.*;

@ApplicationScoped
public class QuarkusAskService implements AskService
{
  private final Assistant assistant;

  public QuarkusAskService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer ask(Question question)
  {
    return new Answer(assistant.ask(question.question()));
  }
}
