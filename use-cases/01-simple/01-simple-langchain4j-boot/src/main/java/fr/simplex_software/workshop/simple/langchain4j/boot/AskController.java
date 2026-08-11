package fr.simplex_software.workshop.simple.langchain4j.boot;

import fr.simplex_software.workshop.simple.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

/**
 * Byte-for-byte the same as the Spring AI track's controller apart from its package. That is the
 * finding, not an oversight: on Spring Boot, the choice of AI library changes nothing above the
 * service layer.
 */
@RestController
public class AskController
{
  private final QuestionAnsweringService questionAnsweringService;

  public AskController(QuestionAnsweringService questionAnsweringService)
  {
    this.questionAnsweringService = questionAnsweringService;
  }

  @PostMapping(path = "/ask", produces = "application/json")
  public Answer ask(@Valid @RequestBody Question question)
  {
    return questionAnsweringService.askQuestion(question);
  }
}
