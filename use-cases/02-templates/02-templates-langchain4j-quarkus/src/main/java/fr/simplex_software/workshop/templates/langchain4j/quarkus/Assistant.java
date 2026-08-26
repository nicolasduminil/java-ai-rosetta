package fr.simplex_software.workshop.templates.langchain4j.quarkus;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.templates.api.*;
import io.quarkiverse.langchain4j.*;

/**
 * The Quarkus AI service. The templated {@code @SystemMessage} references the method parameters by
 * name ({@code {topic}}, {@code {reference}}), the structured {@link Answer} is produced from the
 * return type, and {@link Result} carries token usage. Unlike the Spring Boot track there is no
 * hand-written model configuration: {@code @RegisterAiService} wires the auto-configured Ollama
 * {@code ChatModel} for us.
 */
@RegisterAiService
public interface Assistant
{
  @SystemMessage("""
    You are a helpful assistant answering a question about {topic}.

    If the REFERENCE section below is not empty, treat it as the authoritative source and answer
    strictly from it. Otherwise, answer from your general knowledge. Answer concisely.

    Set the "topic" field of your answer to: {topic}

    REFERENCE
    ---------
    {reference}
    """)
  Result<Answer> answer(@V("topic") String topic,
                        @V("reference") String reference,
                        @UserMessage String question);
}
