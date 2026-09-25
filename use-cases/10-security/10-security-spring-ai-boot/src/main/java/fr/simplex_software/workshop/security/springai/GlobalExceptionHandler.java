package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.tool.execution.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler
{
  @ExceptionHandler(GuardrailViolationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onGuardrail(GuardrailViolationException e)
  {
    return Map.of("error", e.getMessage());
  }

  @ExceptionHandler(ContentModerationException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onModeration(ContentModerationException e)
  {
    return Map.of("error", e.getMessage());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  Map<String, String> onAccessDenied(AccessDeniedException e)
  {
    return Map.of("error", "access denied");
  }

  @ExceptionHandler(ToolExecutionException.class)
  ResponseEntity<Map<String, String>> onToolExecution(ToolExecutionException e)
  {
    if (e.getCause() instanceof AccessDeniedException)
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "access denied"));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "tool execution failed"));
  }
}
