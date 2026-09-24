package fr.simplex_software.workshop.generative.langchain4j.quarkus;

import fr.simplex_software.workshop.generative.api.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GenerateResource
{
  private final GenerativeService generativeService;

  public GenerateResource(GenerativeService generativeService)
  {
    this.generativeService = generativeService;
  }

  @POST
  @Path("summarize")
  public Summary summarize(@Valid TextRequest request)
  {
    return generativeService.summarize(request);
  }

  @POST
  @Path("translate")
  public Translation translate(@Valid TranslateRequest request)
  {
    return generativeService.translate(request);
  }

  @POST
  @Path("sentiment")
  public SentimentResponse sentiment(@Valid TextRequest request)
  {
    return generativeService.analyzeSentiment(request);
  }

  @POST
  @Path("extract")
  public Extraction extract(@Valid TextRequest request)
  {
    return generativeService.extract(request);
  }
}
