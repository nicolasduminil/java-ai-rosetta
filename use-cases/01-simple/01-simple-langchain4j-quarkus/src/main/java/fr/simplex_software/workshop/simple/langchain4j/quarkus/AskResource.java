package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.api.*;
// Not jakarta.validation.*: it declares a Path type that collides with jakarta.ws.rs.Path.
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

/**
 * The JAX-RS counterpart of the two Spring tracks' {@code AskController}. Same path, same media
 * type, same status codes - the conformance suite cannot tell the three apart over the wire.
 */
@Path("/ask")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AskResource
{
  private final QuestionAnsweringService questionAnsweringService;

  public AskResource(QuestionAnsweringService questionAnsweringService)
  {
    this.questionAnsweringService = questionAnsweringService;
  }

  @POST
  public Answer ask(@Valid Question question)
  {
    return questionAnsweringService.askQuestion(question);
  }
}
