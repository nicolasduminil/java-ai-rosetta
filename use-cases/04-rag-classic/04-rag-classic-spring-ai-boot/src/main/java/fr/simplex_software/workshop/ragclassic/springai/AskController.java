package fr.simplex_software.workshop.ragclassic.springai;

import fr.simplex_software.workshop.rag.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

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
