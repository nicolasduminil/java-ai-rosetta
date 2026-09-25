package fr.simplex_software.workshop.memory.persistent.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

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
