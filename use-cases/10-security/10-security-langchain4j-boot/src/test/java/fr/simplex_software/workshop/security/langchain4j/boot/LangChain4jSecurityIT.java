package fr.simplex_software.workshop.security.langchain4j.boot;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.security.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;
import org.springframework.test.context.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LangChain4jSecurityIT extends AbstractSecurityIT
{
  @LocalServerPort
  private int port;

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry)
  {
    registry.add("langchain4j.ollama.chat-model.base-url", OllamaTestSupport::baseUrl);
    registry.add("langchain4j.ollama.chat-model.model-name", OllamaTestSupport::model);
    registry.add("langchain4j.ollama.embedding-model.base-url", OllamaTestSupport::baseUrl);
    registry.add("langchain4j.ollama.embedding-model.model-name", OllamaTestSupport::embeddingModel);
    registry.add("langchain4j.qdrant.host", QdrantTestSupport::host);
    registry.add("langchain4j.qdrant.port", QdrantTestSupport::grpcPort);
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
