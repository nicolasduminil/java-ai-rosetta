package fr.simplex_software.workshop.mcp.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
