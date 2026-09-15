package fr.simplex_software.workshop.tools.langchain4j.quarkus;

import fr.simplex_software.workshop.tools.api.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/ask")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AskResource
{
  private final AskService askService;

  public AskResource(AskService askService)
  {
    this.askService = askService;
  }

  @POST
  public Answer ask(@Valid Question question)
  {
    return askService.ask(question);
  }
}
