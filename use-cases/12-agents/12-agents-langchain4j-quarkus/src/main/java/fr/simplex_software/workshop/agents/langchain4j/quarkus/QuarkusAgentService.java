package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import fr.simplex_software.workshop.agents.api.*;
import io.smallrye.mutiny.*;
import io.smallrye.mutiny.infrastructure.*;
import jakarta.enterprise.context.*;

import java.util.*;

@ApplicationScoped
public class QuarkusAgentService implements AgentService
{
  private final Assistant assistant;
  private final Planner planner;

  public QuarkusAgentService(Assistant assistant, Planner planner)
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
    Category category = assistant.classify(request.text()).category();
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
    Uni<String> summary = Uni.createFrom().item(() -> assistant.summarize(text))
      .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    Uni<Sentiment> sentiment = Uni.createFrom().item(() -> assistant.sentiment(text).sentiment())
      .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    Uni<List<String>> keyPoints = Uni.createFrom().item(() -> splitPoints(assistant.keyPoints(text)))
      .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    return Uni.combine().all().unis(summary, sentiment, keyPoints)
      .with((s, se, kp) -> new AnalysisReport(s, se, kp))
      .await().indefinitely();
  }

  @Override
  public PlanResult plan(PlanRequest request)
  {
    return new PlanResult(planner.plan(request.goal()));
  }

  private static List<String> splitPoints(String text)
  {
    return text.lines()
      .map(line -> line.replaceFirst("^[\\s\\-*\\d.)]+", "").trim())
      .filter(line -> !line.isBlank())
      .toList();
  }
}
