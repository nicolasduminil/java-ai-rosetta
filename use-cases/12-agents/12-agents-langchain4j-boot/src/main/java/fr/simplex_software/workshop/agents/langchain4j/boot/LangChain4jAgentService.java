package fr.simplex_software.workshop.agents.langchain4j.boot;

import fr.simplex_software.workshop.agents.api.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

@Service
public class LangChain4jAgentService implements AgentService
{
  private final Assistant assistant;
  private final Planner planner;

  public LangChain4jAgentService(Assistant assistant, Planner planner)
  {
    this.assistant = assistant;
    this.planner = planner;
  }

  @Override
  public ChainResult chain(TextRequest request)
  {
    String summary = assistant.tighten(assistant.extractFacts(request.text()));
    return new ChainResult(assistant.headline(summary), summary);
  }

  @Override
  public RouteResult route(TextRequest request)
  {
    Category category = assistant.classify(request.text());
    String role = switch (category)
    {
      case BILLING -> "billing";
      case TECHNICAL -> "technical";
      case GENERAL -> "general";
    };
    return new RouteResult(category, assistant.reply(role, request.text()));
  }

  @Override
  public AnalysisReport analyze(TextRequest request)
  {
    String text = request.text();
    try (var executor = Executors.newVirtualThreadPerTaskExecutor())
    {
      CompletableFuture<String> summary = CompletableFuture.supplyAsync(() -> assistant.summarize(text), executor);
      CompletableFuture<Sentiment> sentiment = CompletableFuture.supplyAsync(() -> assistant.sentiment(text), executor);
      CompletableFuture<Assistant.KeyPoints> keyPoints = CompletableFuture.supplyAsync(() -> assistant.keyPoints(text), executor);
      return new AnalysisReport(summary.join(), sentiment.join(), keyPoints.join().keyPoints());
    }
  }

  @Override
  public PlanResult plan(PlanRequest request)
  {
    return new PlanResult(planner.plan(request.goal()));
  }
}
