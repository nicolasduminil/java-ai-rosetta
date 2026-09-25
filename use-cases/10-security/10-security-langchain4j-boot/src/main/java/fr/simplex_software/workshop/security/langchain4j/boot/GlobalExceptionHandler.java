package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.guardrail.*;
import dev.langchain4j.service.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
