package fr.simplex_software.workshop.security.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
