package fr.simplex_software.workshop.templates.langchain4j.boot;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.templates.api.*;

public interface Assistant
{
  @SystemMessage(fromResource = "prompts/systemPromptTemplate.txt")
  Result<Answer> answer(@V("topic") String topic,
                        @V("reference") String reference,
                        @UserMessage String question);
}
