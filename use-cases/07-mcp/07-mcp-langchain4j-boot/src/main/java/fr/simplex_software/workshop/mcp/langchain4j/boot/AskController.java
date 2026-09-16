package fr.simplex_software.workshop.mcp.langchain4j.boot;

import fr.simplex_software.workshop.mcp.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AskController
{
  private final AskService askService;

  public AskController(AskService askService)
  {
    this.askService = askService;
  }

  @PostMapping(path = "/ask", produces = "application/json")
  public Answer ask(@Valid @RequestBody Question question)
  {
    return askService.ask(question);
  }
}
