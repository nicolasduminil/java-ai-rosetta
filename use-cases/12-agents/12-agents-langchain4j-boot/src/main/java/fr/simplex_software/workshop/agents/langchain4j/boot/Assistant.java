package fr.simplex_software.workshop.agents.langchain4j.boot;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.agents.api.*;

import java.util.*;

public interface Assistant
{
  @SystemMessage("Extract the key facts from the text as a short bulleted list.")
  @UserMessage("{{text}}")
  String extractFacts(@V("text") String text);

  @SystemMessage("Write a single tight paragraph summarising these facts.")
  @UserMessage("{{facts}}")
  String tighten(@V("facts") String facts);

  @SystemMessage("Write a short headline of a few words for this summary. Reply with the headline only.")
  @UserMessage("{{summary}}")
  String headline(@V("summary") String summary);

  @SystemMessage("Classify the support request into exactly one category: BILLING, TECHNICAL or GENERAL.")
  @UserMessage("{{text}}")
  Category classify(@V("text") String text);

  @SystemMessage("You are a {{role}} support agent. Answer the request helpfully and concisely.")
  @UserMessage("{{text}}")
  String reply(@V("role") String role, @V("text") String text);

  @SystemMessage("Summarise the text in one sentence.")
  @UserMessage("{{text}}")
  String summarize(@V("text") String text);

  @SystemMessage("Classify the overall sentiment of the text.")
  @UserMessage("{{text}}")
  Sentiment sentiment(@V("text") String text);

  @SystemMessage("List three to five short key points from the text.")
  @UserMessage("{{text}}")
  KeyPoints keyPoints(@V("text") String text);

  record KeyPoints(List<String> keyPoints)
  {
  }
}
