package fr.simplex_software.workshop.simple.springai;

import fr.simplex_software.workshop.simple.conformance.AbstractQuestionAnsweringIT;
import fr.simplex_software.workshop.simple.conformance.OllamaTestSupport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiQuestionAnsweringIT extends AbstractQuestionAnsweringIT
{

  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void ollamaProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.ai.ollama.base-url", OllamaTestSupport::baseUrl);
    // chat.model, not the deprecated chat.options.model, and the same spelling the application
    // uses. Both resolve to the same value here, so a mismatch would have passed unnoticed.
    registry.add("spring.ai.ollama.chat.model", OllamaTestSupport::model);
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
