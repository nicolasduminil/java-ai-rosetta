package fr.simplex_software.workshop.observability.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
