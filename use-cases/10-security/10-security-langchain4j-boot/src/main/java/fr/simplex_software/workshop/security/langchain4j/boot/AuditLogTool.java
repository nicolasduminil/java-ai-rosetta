package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.agent.tool.*;
import org.springframework.security.access.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

/**
 * A tool the assistant may call, restricted to administrators. Unlike Spring AI, LangChain4j discovers
 * {@code @Tool} by reflecting over the bean's own class, and a method-security annotation such as
 * {@code @PreAuthorize} would make Spring wrap the bean in a CGLIB proxy whose methods no longer carry
 * {@code @Tool}, so the tool would not be found. The authorization is therefore enforced imperatively here,
 * reading the security context; a non-admin call throws, which the controller advice turns into HTTP 403.
 */
@Component
public class AuditLogTool
{
  @Tool("Returns the most recent entry from the internal audit log. Administrators only.")
  public String readAuditLog()
  {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
      .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin)
      throw new AccessDeniedException("the audit log is restricted to administrators");
    return "2026-09-22T10:15:00Z user=admin action=login result=success";
  }
}
