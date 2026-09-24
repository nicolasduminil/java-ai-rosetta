package fr.simplex_software.workshop.generative.langchain4j.boot;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.generative.api.*;

public interface TextAssistant
{
  @SystemMessage("You summarise text. Reply with a short headline and three to five concise key points.")
  @UserMessage("Summarise the following text:\n\n{{text}}")
  Summary summarize(@V("text") String text);

  @UserMessage("Detect the source language of the text below and translate it into {{lang}}.\n\nText:\n{{text}}")
  Translation translate(@V("text") String text, @V("lang") String targetLanguage);

  @SystemMessage("You are a sentiment classifier.")
  @UserMessage("Classify the overall sentiment of this text:\n\n{{text}}")
  Sentiment analyzeSentiment(@V("text") String text);

  @UserMessage("Extract the people, organizations and email addresses mentioned in the text below.\n\nText:\n{{text}}")
  Extraction extract(@V("text") String text);
}
