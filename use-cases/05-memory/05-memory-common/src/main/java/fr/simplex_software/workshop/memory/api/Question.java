package fr.simplex_software.workshop.memory.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String conversationId, @NotBlank String question)
{
}
