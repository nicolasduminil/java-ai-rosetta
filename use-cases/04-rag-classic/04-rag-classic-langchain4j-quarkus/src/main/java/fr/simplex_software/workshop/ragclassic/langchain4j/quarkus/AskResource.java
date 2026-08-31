package fr.simplex_software.workshop.ragclassic.langchain4j.quarkus;

import fr.simplex_software.workshop.ragclassic.api.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

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
