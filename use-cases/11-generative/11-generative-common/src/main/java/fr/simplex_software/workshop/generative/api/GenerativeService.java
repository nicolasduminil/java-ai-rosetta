package fr.simplex_software.workshop.generative.api;

public interface GenerativeService
{
  Summary summarize(TextRequest request);

  Translation translate(TranslateRequest request);

  SentimentResponse analyzeSentiment(TextRequest request);

  Extraction extract(TextRequest request);
}
