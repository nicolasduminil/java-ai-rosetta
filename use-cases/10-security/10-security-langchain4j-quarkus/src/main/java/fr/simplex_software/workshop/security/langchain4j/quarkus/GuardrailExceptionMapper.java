package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.guardrail.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.util.*;

/**
 * A failed input or output guardrail throws a {@link GuardrailException}; map it to HTTP 403 so the refusal
 * matches the contract shared with the Spring stacks.
 */
@Provider
public class GuardrailExceptionMapper implements ExceptionMapper<GuardrailException>
{
  @Override
  public Response toResponse(GuardrailException exception)
  {
    return Response.status(Response.Status.FORBIDDEN)
      .entity(Map.of("error", "request refused by a guardrail"))
      .build();
  }
}
