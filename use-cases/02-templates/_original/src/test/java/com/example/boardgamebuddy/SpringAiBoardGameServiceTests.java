package com.example.boardgamebuddy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class SpringAiBoardGameServiceTests {

  @Autowired
  private BoardGameService boardGameService;

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  @Test
  public void evaluateRelevancy() {
    var userText = "How many pieces are there?";
    var game = "Checkers";
    var question = new Question(game, userText);
    var answer = boardGameService.askQuestion(question);
    var relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
    var response = relevancyEvaluator.evaluate(new EvaluationRequest(userText, List.of(), answer.answer()));
    Assertions.assertThat(response.isPass())
        .withFailMessage("""
          ========================================
          The answer "%s"
          is not considered relevant to the question
          "%s".
          ========================================
          """, answer.answer(), userText)
        .isTrue();
  }

}
