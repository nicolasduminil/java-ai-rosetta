package fr.simplex_software.workshop.conformance.langchain4j;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.chat.request.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import fr.simplex_software.workshop.eval.api.*;

/**
 * Adapts the LangChain4j {@link Judge} to the neutral {@link AnswerEvaluator}. The judge model is
 * built by hand (rather than through the framework) so it runs on the larger judge model, with
 * grammar-constrained JSON so the tiny-to-modest judge still returns a parseable verdict.
 */
public final class LangChain4jAnswerEvaluator implements AnswerEvaluator
{
  private final Judge judge;

  private LangChain4jAnswerEvaluator(Judge judge)
  {
    this.judge = judge;
  }

  public static AnswerEvaluator create(String baseUrl, String judgeModel)
  {
    var model = OllamaChatModel.builder()
      .baseUrl(baseUrl)
      .modelName(judgeModel)
      .temperature(0.0)
      .responseFormat(ResponseFormat.JSON)
      .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
      .build();
    return new LangChain4jAnswerEvaluator(AiServices.builder(Judge.class).chatModel(model).build());
  }

  @Override
  public Evaluation relevancy(String question, String answer)
  {
    return judge.relevancy(question, answer);
  }

  @Override
  public Evaluation factualAccuracy(String question, String answer, String reference)
  {
    return judge.factualAccuracy(answer, reference);
  }
}
