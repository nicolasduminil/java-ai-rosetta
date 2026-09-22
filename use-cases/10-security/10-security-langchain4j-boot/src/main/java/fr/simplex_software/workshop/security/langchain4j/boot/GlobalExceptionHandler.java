package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.guardrail.*;
import dev.langchain4j.service.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Maps every security violation to HTTP 403. LangChain4j throws a {@link GuardrailException} (input or
 * output) when a guardrail fails, the service throws a {@link ModerationException} when the moderation model
 * flags the input, and a denied tool call surfaces as an {@link AccessDeniedException}. Validation and
 * malformed-body errors keep Spring Boot's default 400.
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
  @ExceptionHandler(GuardrailException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onGuardrail(GuardrailException e)
  {
    return Map.of("error", "request refused by a guardrail");
  }

  @ExceptionHandler(ModerationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onModeration(ModerationException e)
  {
    return Map.of("error", "content flagged by moderation");
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onAccessDenied(AccessDeniedException e)
  {
    return Map.of("error", "access denied");
  }
}
