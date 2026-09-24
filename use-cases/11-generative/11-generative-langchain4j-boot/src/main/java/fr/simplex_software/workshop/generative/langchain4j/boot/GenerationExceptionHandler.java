package fr.simplex_software.workshop.generative.langchain4j.boot;

import dev.langchain4j.service.output.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GenerationExceptionHandler
{
  @ExceptionHandler(OutputParsingException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  Map<String, String> onParseFailure(OutputParsingException e)
  {
    return Map.of("error", "the model returned output that could not be parsed");
  }
}
