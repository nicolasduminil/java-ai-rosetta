package fr.simplex_software.workshop.templates.langchain4j.boot;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.templates.api.*;

/**
 * The declarative counterpart to Spring AI's fluent call chain: the prompt is a templated
 * {@code @SystemMessage}, the structured {@link Answer} is produced from the method's return type,
 * and {@link Result} additionally exposes token usage. This interface is turned into a bean by
 * {@link ChatModelConfig} via {@code AiServices}.
 */
public interface Assistant
{
  @SystemMessage(fromResource = "prompts/systemPromptTemplate.txt")
  Result<Answer> answer(@V("topic") String topic,
                        @V("reference") String reference,
                        @UserMessage String question);
}
