package fr.simplex_software.workshop.simple.springai;

import fr.simplex_software.workshop.simple.api.Answer;
import fr.simplex_software.workshop.simple.api.Question;
import fr.simplex_software.workshop.simple.api.QuestionAnsweringService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * The Spring AI half of the comparison, and the only class in this module that touches an AI API.
 *
 * <p>Spring AI's model is a fluent {@link ChatClient} built from an injected builder: the starter
 * auto-configures the builder from {@code spring.ai.ollama.*}, so no model, endpoint or transport
 * appears in the code.
 */
@Service
public class SpringAiQuestionAnsweringService implements QuestionAnsweringService {

  private final ChatClient chatClient;

  public SpringAiQuestionAnsweringService(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Answer askQuestion(Question question) {
    var answerText = chatClient.prompt()
        .user(question.question())
        .call()
        .content();
    return new Answer(answerText);
  }
}
