package fr.simplex_software.workshop.eval.springai;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.eval.api.*;
import fr.simplex_software.workshop.eval.conformance.*;
import fr.simplex_software.workshop.templates.springai.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.ollama.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(classes = QuestionAnsweringApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiAnswerEvaluationIT extends AbstractAnswerEvaluationIT
{
  @LocalServerPort
  private int port;

  @Autowired
  private ChatClient.Builder chatClientBuilder;

  @DynamicPropertySource
  static void ollamaProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.ai.ollama.base-url", OllamaTestSupport::baseUrl);
    registry.add("spring.ai.ollama.chat.model", OllamaTestSupport::model);
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }

  @Override
  protected AnswerEvaluator createEvaluator()
  {
    return new SpringAiAnswerEvaluator(chatClientBuilder.defaultOptions(
      OllamaChatOptions.builder().model(OllamaTestSupport.judgeModel()).temperature(0.0)));
  }
}
