package fr.simplex_software.workshop.simple.api;

import jakarta.validation.constraints.NotBlank;

public record Question(@NotBlank String question)
{
}
