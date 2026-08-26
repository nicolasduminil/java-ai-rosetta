package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.conformance.*;
import io.quarkus.test.common.*;

import java.util.*;

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
