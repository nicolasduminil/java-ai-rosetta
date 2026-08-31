package fr.simplex_software.workshop.conformance.quarkus;

import fr.simplex_software.workshop.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

/**
 * Wires the shared Qdrant container and the embedding model into a Quarkus RAG test, alongside
 * {@link OllamaTestResource} which supplies the chat model. Requesting the embedding model here pulls
 * it on first use, so only RAG use cases pay for it.
 */
public class RagStoreTestResource implements QuarkusTestResourceLifecycleManager
{
  @Override
  public Map<String, String> start()
  {
    return Map.of(
      "quarkus.langchain4j.ollama.embedding-model.model-id", OllamaTestSupport.embeddingModel(),
      "quarkus.langchain4j.qdrant.host", QdrantTestSupport.host(),
      "quarkus.langchain4j.qdrant.port", String.valueOf(QdrantTestSupport.grpcPort()));
  }

  @Override
  public void stop()
  {
  }
}
