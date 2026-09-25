package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import dev.langchain4j.service.output.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.util.*;

@Provider
public class GenerationExceptionMapper implements ExceptionMapper<OutputParsingException>
{
  @Override
  public Response toResponse(OutputParsingException exception)
  {
    return Response.status(422)
      .entity(Map.of("error", "a step returned output that could not be parsed"))
      .build();
  }
}
