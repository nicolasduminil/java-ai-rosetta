package fr.simplex_software.workshop.templates.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String topic, @NotBlank String question)
{
}
