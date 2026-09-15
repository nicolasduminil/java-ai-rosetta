package fr.simplex_software.workshop.tools.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
