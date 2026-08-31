package fr.simplex_software.workshop.eval.springai;

import fr.simplex_software.workshop.eval.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.evaluation.*;
import org.springframework.ai.document.*;
import org.springframework.ai.evaluation.*;

import java.util.*;

class SpringAiAnswerEvaluator implements AnswerEvaluator
{
  private final RelevancyEvaluator relevancyEvaluator;
  private final FactCheckingEvaluator factCheckingEvaluator;

  SpringAiAnswerEvaluator(ChatClient.Builder judgeChatClientBuilder)
  {
    this.relevancyEvaluator = new RelevancyEvaluator(judgeChatClientBuilder);
    this.factCheckingEvaluator = FactCheckingEvaluator.builder(judgeChatClientBuilder).build();
  }

  @Override
  public Evaluation relevancy(String question, String answer)
  {
    var response = relevancyEvaluator.evaluate(new EvaluationRequest(question, answer));
    return new Evaluation(response.isPass(), response.getFeedback());
  }

  @Override
  public Evaluation factualAccuracy(String question, String answer, String reference)
  {
    var response = factCheckingEvaluator.evaluate(
      new EvaluationRequest(List.of(new Document(reference)), answer));
    return new Evaluation(response.isPass(), response.getFeedback());
  }
}
