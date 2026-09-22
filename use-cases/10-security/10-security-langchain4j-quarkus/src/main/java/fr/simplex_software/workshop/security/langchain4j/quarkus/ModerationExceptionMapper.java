package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.service.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.util.*;

/**
 * When the moderation model flags the input, the service throws a {@link ModerationException}; map it to 403.
 */
@Provider
public class ModerationExceptionMapper implements ExceptionMapper<ModerationException>
{
  @Override
  public Response toResponse(ModerationException exception)
  {
    return Response.status(Response.Status.FORBIDDEN)
      .entity(Map.of("error", "content flagged by moderation"))
      .build();
  }
}
