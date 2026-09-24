package fr.simplex_software.workshop.generative.api;

import jakarta.validation.constraints.*;

public record TextRequest(@NotBlank String text)
{
}
