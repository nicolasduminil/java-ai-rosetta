package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.data.message.*;
import dev.langchain4j.guardrail.*;
import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
public class InjectionGuardrail implements InputGuardrail
{
  private static final List<String> INJECTION_MARKERS = List.of(
    "ignore all previous", "ignore previous instructions", "disregard the above", "disregard previous",
    "forget your instructions", "reveal your system prompt", "reveal every secret", "reveal all secrets");

  @Override
  public InputGuardrailResult validate(UserMessage userMessage)
  {
    if (INJECTION_MARKERS.stream().anyMatch(userMessage.singleText().toLowerCase()::contains))
      return failure("prompt injection detected");
    return success();
  }
}
