package fr.simplex_software.workshop.generative.langchain4j.boot;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.generative.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LangChain4jGenerativeIT extends AbstractGenerativeIT
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
}
