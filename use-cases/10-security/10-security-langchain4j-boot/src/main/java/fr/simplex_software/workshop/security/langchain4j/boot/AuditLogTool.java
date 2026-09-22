package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.agent.tool.*;
import org.springframework.security.access.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

/**
 * A tool the assistant may call, restricted to administrators.
 * The authorization is enforced imperatively here, reading the security context.
 * A non-admin call throws an exception which the controller advice turns into HTTP 403.
 */
@Component
public class AuditLogTool
{
  @Tool("Returns the most recent entry from the internal audit log. Administrators only.")
  public String readAuditLog()
  {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication != null && authentication.getAuthorities().stream()
      .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))))
      throw new AccessDeniedException("the audit log is restricted to administrators");
    return "2026-09-22T10:15:00Z user=admin action=login result=success";
  }
}
