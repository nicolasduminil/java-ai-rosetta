package fr.simplex_software.workshop.agents.api;

import jakarta.validation.constraints.*;

public record PlanRequest(@NotBlank String goal)
{
}
