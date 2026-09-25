package fr.simplex_software.workshop.security.langchain4j.quarkus;

import io.quarkiverse.langchain4j.guardrails.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;

@RequestScoped
public class SecurityToolGuardrail implements ToolInputGuardrail
{
  @Inject
  SecurityIdentity securityIdentity;

  @Override
  public ToolInputGuardrailResult validate(ToolInputGuardrailRequest request)
  {
    if (!securityIdentity.hasRole("ADMIN"))
      return ToolInputGuardrailResult.fatal(
        "unauthorized tool call",
        new ForbiddenException("the " + request.toolName() + " tool is restricted to administrators"));
    return ToolInputGuardrailResult.success();
  }
}
