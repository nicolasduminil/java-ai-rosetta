package fr.simplex_software.workshop.security.langchain4j.quarkus;

import io.quarkiverse.langchain4j.guardrails.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.util.*;

/**
 * A failed tool input guardrail (here, an unauthorized tool call) throws a {@link ToolGuardrailException};
 * map it to HTTP 403 so the contract matches the other stacks.
 */
@Provider
public class ToolGuardrailExceptionMapper implements ExceptionMapper<ToolGuardrailException>
{
  @Override
  public Response toResponse(ToolGuardrailException exception)
  {
    return Response.status(Response.Status.FORBIDDEN)
      .entity(Map.of("error", "tool call not authorized"))
      .build();
  }
}
