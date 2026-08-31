package fr.simplex_software.workshop.templates.langchain4j.quarkus;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.templates.api.*;
import io.quarkiverse.langchain4j.*;

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
