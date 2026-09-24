package fr.simplex_software.workshop.generative.api;

import jakarta.validation.constraints.*;

public record TranslateRequest(@NotBlank String text, @NotBlank String targetLanguage)
{
}
