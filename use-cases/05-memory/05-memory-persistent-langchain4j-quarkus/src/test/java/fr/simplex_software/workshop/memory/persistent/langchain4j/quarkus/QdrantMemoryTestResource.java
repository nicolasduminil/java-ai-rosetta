package fr.simplex_software.workshop.memory.persistent.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

/**
 * Feeds the shared Qdrant container's host and port to the hand-rolled ChatMemoryStore under the plain
 * {@code chat-memory.qdrant.*} keys the application reads (not the {@code quarkus.langchain4j.qdrant.*}
 * keys, which belong to the Qdrant extension this stack does not use).
 */
public class QdrantMemoryTestResource implements QuarkusTestResourceLifecycleManager
{
  @Override
  public Map<String, String> start()
  {
    return Map.of(
      "chat-memory.qdrant.host", QdrantTestSupport.host(),
      "chat-memory.qdrant.port", String.valueOf(QdrantTestSupport.grpcPort()));
  }

  @Override
  public void stop()
  {
  }
}
