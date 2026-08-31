package fr.simplex_software.workshop.eval.langchain4j.boot;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.conformance.langchain4j.*;
import fr.simplex_software.workshop.eval.api.*;
import fr.simplex_software.workshop.eval.conformance.*;
import fr.simplex_software.workshop.templates.langchain4j.boot.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(classes = QuestionAnsweringApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LangChain4jAnswerEvaluationIT extends AbstractAnswerEvaluationIT
{
  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void ollamaProperties(DynamicPropertyRegistry registry)
  {
    registry.add("langchain4j.ollama.chat-model.base-url", OllamaTestSupport::baseUrl);
    registry.add("langchain4j.ollama.chat-model.model-name", OllamaTestSupport::model);
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }

  @Override
  protected AnswerEvaluator createEvaluator()
  {
    return LangChain4jAnswerEvaluator.create(
      OllamaTestSupport.baseUrl(), OllamaTestSupport.judgeModel());
  }
}
