package fr.simplex_software.workshop.rag.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
