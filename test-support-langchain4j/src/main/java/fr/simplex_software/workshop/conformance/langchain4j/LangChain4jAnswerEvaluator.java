package fr.simplex_software.workshop.conformance.langchain4j;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.chat.request.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import fr.simplex_software.workshop.eval.api.*;

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
