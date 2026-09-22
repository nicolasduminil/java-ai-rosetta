package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.data.message.*;
import dev.langchain4j.guardrail.*;
import jakarta.enterprise.context.*;

/**
 * A first-class LangChain4j output guardrail as a CDI bean: it refuses any answer that would leak the
 * forbidden project codename, using {@code fatal} so the refusal is immediate.
 */
@ApplicationScoped
public class LeakGuardrail implements OutputGuardrail
{
  private static final String FORBIDDEN_OUTPUT = "BLUEFIN";

  @Override
  public OutputGuardrailResult validate(AiMessage responseFromLLM)
  {
    String text = responseFromLLM.text();
    if (text != null && text.toUpperCase().contains(FORBIDDEN_OUTPUT))
      return fatal("forbidden content detected");
    return success();
  }
}
