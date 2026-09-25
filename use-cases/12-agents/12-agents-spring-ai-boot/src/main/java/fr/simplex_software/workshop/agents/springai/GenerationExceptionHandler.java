package fr.simplex_software.workshop.agents.springai;

import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.web.bind.annotation.*;

import tools.jackson.core.*;

import java.util.*;

@RestControllerAdvice
public class GenerationExceptionHandler
{
  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String, String> onUnreadableBody(HttpMessageNotReadableException e)
  {
    return Map.of("error", "the request body could not be read");
  }

  @ExceptionHandler(JacksonException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  Map<String, String> onParseFailure(JacksonException e)
  {
    return Map.of("error", "a step returned output that could not be parsed");
  }
}
