package fr.simplex_software.workshop.agents.api;

import jakarta.validation.constraints.*;

public record TextRequest(@NotBlank String text)
{
}
