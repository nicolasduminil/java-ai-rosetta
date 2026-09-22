package fr.simplex_software.workshop.security.langchain4j.quarkus;

import fr.simplex_software.workshop.security.api.*;
import io.quarkus.security.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

/**
 * Every call needs a valid JWT ({@code @Authenticated}); a missing or invalid token yields 401.
 */
@Path("/ask")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
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
