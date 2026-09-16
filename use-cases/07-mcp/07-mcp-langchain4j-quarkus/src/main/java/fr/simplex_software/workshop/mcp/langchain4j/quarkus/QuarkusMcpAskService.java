package fr.simplex_software.workshop.mcp.langchain4j.quarkus;

import fr.simplex_software.workshop.mcp.api.*;
import jakarta.enterprise.context.*;

@ApplicationScoped
public class QuarkusMcpAskService implements AskService
{
  private final Assistant assistant;

  public QuarkusMcpAskService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer ask(Question question)
  {
    return new Answer(assistant.ask(question.question()));
  }
}
