package fr.simplex_software.workshop.ragmodular.api;

import jakarta.validation.constraints.*;

public record Question(@NotBlank String question)
{
}
