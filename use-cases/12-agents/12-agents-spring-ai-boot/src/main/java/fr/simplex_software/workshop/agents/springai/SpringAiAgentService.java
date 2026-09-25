package fr.simplex_software.workshop.agents.springai;

import fr.simplex_software.workshop.agents.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.stereotype.*;

import java.util.*;
import java.util.concurrent.*;

@Service
public class SpringAiAgentService implements AgentService
{
  private final ChatClient chatClient;
  private final Catalog catalog;

  public SpringAiAgentService(ChatClient.Builder chatClientBuilder, Catalog catalog)
  {
    this.chatClient = chatClientBuilder.build();
    this.catalog = catalog;
  }

  @Override
  public ChainResult chain(TextRequest request)
  {
    String facts = chatClient.prompt()
      .system("Extract the key facts from the text as a short bulleted list.")
      .user(user -> user.text("{text}").param("text", request.text()))
      .call().content();
    String summary = chatClient.prompt()
      .system("Write a single tight paragraph summarising these facts.")
      .user(user -> user.text("{facts}").param("facts", facts))
      .call().content();
    String headline = chatClient.prompt()
      .system("Write a short headline of a few words for this summary. Reply with the headline only.")
      .user(user -> user.text("{summary}").param("summary", summary))
      .call().content();
    return new ChainResult(headline, summary);
  }

  @Override
  public RouteResult route(TextRequest request)
  {
    Category category = chatClient.prompt()
      .system("Classify the support request into exactly one category: BILLING, TECHNICAL or GENERAL.")
      .user(user -> user.text("{text}").param("text", request.text()))
      .call().entity(RouteDecision.class).category();
    String answer = switch (category)
    {
      case BILLING -> reply("You are a billing support agent. Answer the request helpfully and concisely.", request.text());
      case TECHNICAL -> reply("You are a technical support agent. Answer the request helpfully and concisely.", request.text());
      case GENERAL -> reply("You are a general support agent. Answer the request helpfully and concisely.", request.text());
    };
    return new RouteResult(category, answer);
  }

  @Override
  public AnalysisReport analyze(TextRequest request)
  {
    String text = request.text();
    try (var executor = Executors.newVirtualThreadPerTaskExecutor())
    {
      CompletableFuture<String> summary = CompletableFuture.supplyAsync(() -> chatClient.prompt()
        .system("Summarise the text in one sentence.")
        .user(user -> user.text("{t}").param("t", text)).call().content(), executor);
      CompletableFuture<Sentiment> sentiment = CompletableFuture.supplyAsync(() -> chatClient.prompt()
        .system("Classify the overall sentiment of the text.")
        .user(user -> user.text("{t}").param("t", text)).call().entity(SentimentResult.class).sentiment(), executor);
      CompletableFuture<List<String>> keyPoints = CompletableFuture.supplyAsync(() -> chatClient.prompt()
        .system("List three to five short key points from the text.")
        .user(user -> user.text("{t}").param("t", text)).call().entity(KeyPoints.class).keyPoints(), executor);
      return new AnalysisReport(summary.join(), sentiment.join(), keyPoints.join());
    }
  }

  @Override
  public PlanResult plan(PlanRequest request)
  {
    String answer = chatClient.prompt()
      .system("You reach the user's goal by calling the available tools when needed, then give the answer.")
      .user(request.goal())
      .tools(catalog)
      .call().content();
    return new PlanResult(answer);
  }

  private String reply(String systemPrompt, String text)
  {
    return chatClient.prompt().system(systemPrompt).user(text).call().content();
  }

  record RouteDecision(Category category)
  {
  }

  record SentimentResult(Sentiment sentiment)
  {
  }

  record KeyPoints(List<String> keyPoints)
  {
  }
}
