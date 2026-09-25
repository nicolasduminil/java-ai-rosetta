package fr.simplex_software.workshop.ragmodular.langchain4j.boot;

import fr.simplex_software.workshop.ragmodular.api.*;
import org.springframework.stereotype.*;

@Service
public class LangChain4jRagService implements QuestionAnsweringService
{
  private final Assistant assistant;

  public LangChain4jRagService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(assistant.answer(question.question()));
  }
}
