package fr.simplex_software.workshop.observability.langchain4j.boot;

import dev.langchain4j.model.chat.listener.*;
import dev.langchain4j.model.output.*;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.*;

import java.util.concurrent.*;

/**
 * LangChain4j core ships no Micrometer or OpenTelemetry instrumentation, so this hand-written listener
 * records the metrics that Spring AI and Quarkus-LangChain4j emit built-in. It records the OpenTelemetry
 * GenAI convention meters, a {@code gen_ai.client.token.usage} counter (tagged input/output/total) and a
 * {@code gen_ai.client.operation} timer, into the injected {@link MeterRegistry}. The start time is stashed
 * in the listener context's shared attribute map on request and read back on response.
 */
@Component
public class TokenUsageChatModelListener implements ChatModelListener
{
  private static final String START_NANOS = "gen_ai.start_nanos";

  private final MeterRegistry meterRegistry;

  public TokenUsageChatModelListener(MeterRegistry meterRegistry)
  {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public void onRequest(ChatModelRequestContext requestContext)
  {
    requestContext.attributes().put(START_NANOS, System.nanoTime());
  }

  @Override
  public void onResponse(ChatModelResponseContext responseContext)
  {
    recordDuration(responseContext.attributes().get(START_NANOS));
    TokenUsage usage = responseContext.chatResponse().tokenUsage();
    if (usage != null)
    {
      countTokens(usage.inputTokenCount(), "input");
      countTokens(usage.outputTokenCount(), "output");
      countTokens(usage.totalTokenCount(), "total");
    }
  }

  private void countTokens(Integer tokens, String type)
  {
    if (tokens != null)
      meterRegistry.counter("gen_ai.client.token.usage", "gen_ai.token.type", type).increment(tokens);
  }

  private void recordDuration(Object startNanos)
  {
    if (startNanos instanceof Long start)
      meterRegistry.timer("gen_ai.client.operation")
        .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
  }
}
