package fr.simplex_software.workshop.simple.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import fr.simplex_software.workshop.simple.api.*;
import org.springframework.stereotype.*;

/**
 * The LangChain4j half of the comparison, and the only class in this module that touches an AI API.
 *
 * <p>LangChain4j's model is a plain object with a method: the starter auto-configures an
 * {@link ChatModel} from {@code langchain4j.ollama.*} and {@code chat(String)} returns the reply
 * directly. Where Spring AI builds a request through a fluent chain, this is a single call - the
 * whole difference between the two tracks is visible in the one statement below.
 */
@Service
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
