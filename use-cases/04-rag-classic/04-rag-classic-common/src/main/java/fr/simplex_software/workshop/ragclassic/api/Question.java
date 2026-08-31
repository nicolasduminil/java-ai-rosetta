package fr.simplex_software.workshop.ragclassic.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
