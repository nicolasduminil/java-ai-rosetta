package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.service.tool.*;
import io.quarkiverse.langchain4j.*;
import jakarta.enterprise.context.*;

/**
 * By default the Quarkus extension compensates a tool error (feeds it back to the model, so a denied tool
 * call still answers 200). This handler rethrows instead, so the audit-log denial propagates and Quarkus
 * maps it to HTTP 403. Carrying the {@code @DefaultToolExecutionErrorHandler} qualifier makes it replace the
 * extension's built-in handler.
 */
@ApplicationScoped
@DefaultToolExecutionErrorHandler
public class RethrowingToolExecutionErrorHandler implements ToolExecutionErrorHandler
{
  @Override
  public ToolErrorHandlerResult handle(Throwable error, ToolErrorContext context)
  {
    if (error instanceof RuntimeException runtimeException)
      throw runtimeException;
    throw new RuntimeException(error);
  }
}
