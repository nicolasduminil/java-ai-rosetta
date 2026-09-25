package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.data.message.*;
import dev.langchain4j.guardrail.*;
import org.springframework.stereotype.*;

@Component
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
