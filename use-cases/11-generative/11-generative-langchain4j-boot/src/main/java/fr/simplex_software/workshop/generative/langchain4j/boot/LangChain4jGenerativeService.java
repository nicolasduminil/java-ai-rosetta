package fr.simplex_software.workshop.generative.langchain4j.boot;

import fr.simplex_software.workshop.generative.api.*;
import org.springframework.stereotype.*;

@Service
public class LangChain4jGenerativeService implements GenerativeService
{
  private final TextAssistant assistant;

  public LangChain4jGenerativeService(TextAssistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Summary summarize(TextRequest request)
  {
    return assistant.summarize(request.text());
  }

  @Override
  public Translation translate(TranslateRequest request)
  {
    return assistant.translate(request.text(), request.targetLanguage());
  }

  @Override
  public SentimentResponse analyzeSentiment(TextRequest request)
  {
    return new SentimentResponse(assistant.analyzeSentiment(request.text()));
  }

  @Override
  public Extraction extract(TextRequest request)
  {
    return assistant.extract(request.text());
  }
}
