package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import dev.langchain4j.model.chat.*;
import fr.simplex_software.workshop.simple.api.*;
import jakarta.enterprise.context.*;

/**
 * Identical in body to the LangChain4j Spring Boot service - same {@code chat(String)} call on the
 * same {@link ChatModel} type. Only the scope annotation differs: {@code @ApplicationScoped}
 * rather than {@code @Service}.
 *
 * <p>Unlike the Spring Boot track, no hand-written producer is needed: the Quarkiverse extension
 * contributes the {@code ChatModel} bean itself, from {@code quarkus.langchain4j.ollama.*}.
 */
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
