package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.tool.annotation.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;

/**
 * A tool the assistant may call. Method security guards it: only a caller whose token carries the ADMIN role
 * may read the audit log. When a non-admin's request drives the model to call it, {@code @PreAuthorize}
 * throws, and because the tool-execution processor rethrows, the denial surfaces as HTTP 403.
 */
@Component
public class AuditLogTool
{
  @Tool(description = "Returns the most recent entry from the internal audit log. Administrators only.")
  @PreAuthorize("hasRole('ADMIN')")
  public String readAuditLog()
  {
    return "2026-09-22T10:15:00Z user=admin action=login result=success";
  }
}
