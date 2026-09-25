package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.tool.annotation.*;
import org.springframework.security.access.prepost.*;
import org.springframework.stereotype.*;

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
