package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import dev.langchain4j.model.chat.*;
import fr.simplex_software.workshop.simple.api.*;
import jakarta.enterprise.context.*;

@ApplicationScoped
public class LangChain4jQuestionAnsweringService implements QuestionAnsweringService
{
  private final ChatModel chatModel;

  public LangChain4jQuestionAnsweringService(ChatModel chatModel)
  {
    this.chatModel = chatModel;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(chatModel.chat(question.question()));
  }
}
