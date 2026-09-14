package fr.simplex_software.workshop.ragclassic.springai;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.rag.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiRagQuestionAnsweringIT extends AbstractRagQuestionAnsweringIT
{
  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void ragProperties(DynamicPropertyRegistry registry)
  {
    registry.add("spring.ai.ollama.base-url", OllamaTestSupport::baseUrl);
    registry.add("spring.ai.ollama.chat.model", OllamaTestSupport::model);
    registry.add("spring.ai.ollama.embedding.model", OllamaTestSupport::embeddingModel);
    registry.add("spring.ai.vectorstore.qdrant.host", QdrantTestSupport::host);
    registry.add("spring.ai.vectorstore.qdrant.port", QdrantTestSupport::grpcPort);
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
