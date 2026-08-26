package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.api.*;
import jakarta.validation.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;

// Not jakarta.validation.*: it declares a Path type that collides with jakarta.ws.rs.Path.

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
