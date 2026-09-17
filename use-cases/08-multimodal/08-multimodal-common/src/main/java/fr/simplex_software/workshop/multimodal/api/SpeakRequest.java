package fr.simplex_software.workshop.multimodal.api;

import jakarta.validation.constraints.*;

public record SpeakRequest(@NotBlank String text)
{
}
