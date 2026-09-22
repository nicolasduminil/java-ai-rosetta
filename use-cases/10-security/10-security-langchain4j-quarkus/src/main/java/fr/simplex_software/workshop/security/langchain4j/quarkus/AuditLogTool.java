package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.agent.tool.*;
import io.quarkus.security.identity.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import jakarta.ws.rs.*;

/**
 * A tool the assistant may call, restricted to administrators. A {@code @RolesAllowed} annotation is not
 * enforced here: the LangChain4j tool executor invokes the tool bypassing the CDI security interceptor, so
 * (as on the Boot stack, where a CGLIB proxy hides {@code @Tool}) the role is checked imperatively against
 * the injected {@link SecurityIdentity}. A non-admin call throws a 403, which the rethrowing tool-error
 * handler propagates instead of feeding back to the model.
 */
@ApplicationScoped
public class AuditLogTool
{
  private final SecurityIdentity securityIdentity;

  @Inject
  public AuditLogTool(SecurityIdentity securityIdentity)
  {
    this.securityIdentity = securityIdentity;
  }

  @Tool("Returns the most recent entry from the internal audit log. Administrators only.")
  public String readAuditLog()
  {
    if (!securityIdentity.hasRole("ADMIN"))
      throw new ForbiddenException("the audit log is restricted to administrators");
    return "2026-09-22T10:15:00Z user=admin action=login result=success";
  }
}
