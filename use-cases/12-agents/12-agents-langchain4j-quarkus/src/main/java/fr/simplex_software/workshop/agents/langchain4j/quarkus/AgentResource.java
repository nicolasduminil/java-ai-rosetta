package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import fr.simplex_software.workshop.agents.api.*;
import io.smallrye.common.annotation.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AgentResource
{
  private final AgentService agentService;

  public AgentResource(AgentService agentService)
  {
    this.agentService = agentService;
  }

  @POST
  @Path("chain")
  @Blocking
  public ChainResult chain(@Valid TextRequest request)
  {
    return agentService.chain(request);
  }

  @POST
  @Path("route")
  @Blocking
  public RouteResult route(@Valid TextRequest request)
  {
    return agentService.route(request);
  }

  @POST
  @Path("analyze")
  @Blocking
  public AnalysisReport analyze(@Valid TextRequest request)
  {
    return agentService.analyze(request);
  }

  @POST
  @Path("plan")
  @Blocking
  public PlanResult plan(@Valid PlanRequest request)
  {
    return agentService.plan(request);
  }
}
