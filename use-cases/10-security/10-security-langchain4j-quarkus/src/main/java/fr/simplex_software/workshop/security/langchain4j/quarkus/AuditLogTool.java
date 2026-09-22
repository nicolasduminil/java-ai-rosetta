package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.agent.tool.*;
import io.quarkiverse.langchain4j.guardrails.*;
import jakarta.enterprise.context.*;

/**
 * A tool the assistant may call, restricted to administrators. Authorization is enforced by a tool input
 * guardrail ({@link SecurityToolGuardrail}), the mechanism the Quarkus extension provides for securing tool
 * calls; a non-admin call is blocked before the method runs and surfaces as HTTP 403.
 */
@ApplicationScoped
public class AuditLogTool
{
  @Tool("Returns the most recent entry from the internal audit log. Administrators only.")
  @ToolInputGuardrails(SecurityToolGuardrail.class)
  public String readAuditLog()
  {
    return "2026-09-22T10:15:00Z user=admin action=login result=success";
  }
}
