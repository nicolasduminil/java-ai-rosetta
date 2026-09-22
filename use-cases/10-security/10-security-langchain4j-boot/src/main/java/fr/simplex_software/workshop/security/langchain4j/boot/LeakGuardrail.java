package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.data.message.*;
import dev.langchain4j.guardrail.*;
import org.springframework.stereotype.*;

/**
 * A first-class LangChain4j output guardrail: it inspects the model's answer and refuses any response that
 * would leak the forbidden project codename. It uses {@code fatal} so the refusal is immediate (no retry),
 * making the AI service throw an {@link OutputGuardrailException} that becomes HTTP 403.
 */
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
