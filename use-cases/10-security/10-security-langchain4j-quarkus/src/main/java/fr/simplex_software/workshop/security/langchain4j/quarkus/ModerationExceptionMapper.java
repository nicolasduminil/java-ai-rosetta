package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.service.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.util.*;

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
