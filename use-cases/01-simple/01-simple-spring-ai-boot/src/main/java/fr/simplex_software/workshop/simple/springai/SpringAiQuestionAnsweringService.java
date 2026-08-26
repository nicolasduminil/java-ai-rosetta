package fr.simplex_software.workshop.simple.springai;

import fr.simplex_software.workshop.simple.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiQuestionAnsweringService implements QuestionAnsweringService
{

  private final ChatClient chatClient;

  public SpringAiQuestionAnsweringService(ChatClient.Builder chatClientBuilder)
  {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Answer askQuestion(Question question)
  {
    var answerText = chatClient.prompt()
      .user(question.question())
      .call()
      .content();
    return new Answer(answerText);
  }
}
