package fr.simplex_software.workshop.generative.langchain4j.boot;

import fr.simplex_software.workshop.generative.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class GenerateController
{
  private final GenerativeService generativeService;

  public GenerateController(GenerativeService generativeService)
  {
    this.generativeService = generativeService;
  }

  @PostMapping(path = "/summarize", produces = "application/json")
  public Summary summarize(@Valid @RequestBody TextRequest request)
  {
    return generativeService.summarize(request);
  }

  @PostMapping(path = "/translate", produces = "application/json")
  public Translation translate(@Valid @RequestBody TranslateRequest request)
  {
    return generativeService.translate(request);
  }

  @PostMapping(path = "/sentiment", produces = "application/json")
  public SentimentResponse sentiment(@Valid @RequestBody TextRequest request)
  {
    return generativeService.analyzeSentiment(request);
  }

  @PostMapping(path = "/extract", produces = "application/json")
  public Extraction extract(@Valid @RequestBody TextRequest request)
  {
    return generativeService.extract(request);
  }
}
