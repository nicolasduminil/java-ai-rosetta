package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Spring AI advisor that runs around the model call: before, it refuses obvious prompt-injection input;
 * after, it refuses any answer that would leak the forbidden project codename.
 * Either refusal throws, which the controller advice turns into HTTP 403.
 */
@Component
public class SecurityGuardAdvisor implements CallAdvisor
{
  private static final List<String> INJECTION_MARKERS = List.of(
    "ignore all previous", "ignore previous instructions", "disregard the above", "disregard previous",
    "forget your instructions", "reveal your system prompt", "reveal every secret", "reveal all secrets");
  private static final String FORBIDDEN_OUTPUT = "BLUEFIN";

  @Override
  public String getName()
  {
    return "securityGuard";
  }

  @Override
  public int getOrder()
  {
    return 0;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain)
  {
    if (INJECTION_MARKERS.stream().anyMatch(request.prompt().getUserMessage().getText().toLowerCase()::contains))
      throw new GuardrailViolationException("input guardrail: prompt injection detected");

    ChatClientResponse response = chain.nextCall(request);

    String output = response.chatResponse().getResult().getOutput().getText();
    if (output != null && output.toUpperCase().contains(FORBIDDEN_OUTPUT))
      throw new GuardrailViolationException("output guardrail: forbidden content detected");

    return response;
  }
}
