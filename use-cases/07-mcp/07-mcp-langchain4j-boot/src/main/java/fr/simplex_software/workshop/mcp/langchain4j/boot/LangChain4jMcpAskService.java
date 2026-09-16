package fr.simplex_software.workshop.mcp.langchain4j.boot;

import fr.simplex_software.workshop.mcp.api.*;
import org.springframework.stereotype.*;

@Service
public class LangChain4jMcpAskService implements AskService
{
  private final Assistant assistant;

  public LangChain4jMcpAskService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer ask(Question question)
  {
    return new Answer(assistant.ask(question.question()));
  }
}
