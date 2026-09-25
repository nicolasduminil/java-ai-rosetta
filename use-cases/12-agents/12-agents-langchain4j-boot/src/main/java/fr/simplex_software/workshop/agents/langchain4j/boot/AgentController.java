package fr.simplex_software.workshop.agents.langchain4j.boot;

import fr.simplex_software.workshop.agents.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AgentController
{
  private final AgentService agentService;

  public AgentController(AgentService agentService)
  {
    this.agentService = agentService;
  }

  @PostMapping(path = "/chain", produces = "application/json")
  public ChainResult chain(@Valid @RequestBody TextRequest request)
  {
    return agentService.chain(request);
  }

  @PostMapping(path = "/route", produces = "application/json")
  public RouteResult route(@Valid @RequestBody TextRequest request)
  {
    return agentService.route(request);
  }

  @PostMapping(path = "/analyze", produces = "application/json")
  public AnalysisReport analyze(@Valid @RequestBody TextRequest request)
  {
    return agentService.analyze(request);
  }

  @PostMapping(path = "/plan", produces = "application/json")
  public PlanResult plan(@Valid @RequestBody PlanRequest request)
  {
    return agentService.plan(request);
  }
}
