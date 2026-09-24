package fr.simplex_software.workshop.generative.springai;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.generative.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiGenerativeIT extends AbstractGenerativeIT
{
  @LocalServerPort
  private int port;

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
}
