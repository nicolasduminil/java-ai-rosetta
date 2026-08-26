package fr.simplex_software.workshop.simple.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
