package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

/**
 * Feeds the shared Ollama container's address into Quarkus configuration before the application
 * starts. It is the Quarkus equivalent of the Spring tracks' {@code @DynamicPropertySource}, and
 * it points at the very same {@link OllamaTestSupport} container - all three stacks are tested
 * against one model instance, not three.
 *
 * <p>Nothing is stopped here: the container is a JVM-wide singleton reaped by Testcontainers.
 */
public class OllamaTestResource implements QuarkusTestResourceLifecycleManager
{
  @Override
  public Map<String, String> start()
  {
    return Map.of(
      "quarkus.langchain4j.ollama.base-url", OllamaTestSupport.baseUrl(),
      "quarkus.langchain4j.ollama.chat-model.model-id", OllamaTestSupport.model());
  }

  @Override
  public void stop()
  {
  }
}
