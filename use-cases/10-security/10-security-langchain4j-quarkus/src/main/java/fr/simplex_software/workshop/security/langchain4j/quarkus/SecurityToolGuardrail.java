package fr.simplex_software.workshop.security.langchain4j.quarkus;

import io.quarkiverse.langchain4j.guardrails.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;

/**
 * The idiomatic Quarkus way to authorize a tool call: a tool input guardrail that inspects the caller's
 * {@link SecurityIdentity} before the tool runs. This is what the extension provides for tool authorization,
 * which is why a {@code @RolesAllowed} on the tool method itself is not the right tool (it is not enforced by
 * the AI-service tool executor). A non-admin call returns a {@code fatal} result, which hard-blocks the call
 * (a plain {@code failure} would be reported to the model and let it answer 200); the carried
 * {@link ForbiddenException} makes it HTTP 403.
 */
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
