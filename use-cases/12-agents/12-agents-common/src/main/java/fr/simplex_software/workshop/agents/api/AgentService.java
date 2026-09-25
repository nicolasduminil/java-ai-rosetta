package fr.simplex_software.workshop.agents.api;

public interface AgentService
{
  ChainResult chain(TextRequest request);

  RouteResult route(TextRequest request);

  AnalysisReport analyze(TextRequest request);

  PlanResult plan(PlanRequest request);
}
