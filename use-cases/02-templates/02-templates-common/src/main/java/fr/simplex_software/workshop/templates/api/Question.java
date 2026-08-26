package fr.simplex_software.workshop.templates.api;

import jakarta.validation.constraints.*;

/**
 * A question about a {@code topic}. The topic is any subject, not necessarily one this application
 * carries reference material for - when it does, that material is folded into the prompt; when it
 * does not, the model answers from general knowledge.
 */
public record Question(@NotBlank String topic, @NotBlank String question)
{
}
