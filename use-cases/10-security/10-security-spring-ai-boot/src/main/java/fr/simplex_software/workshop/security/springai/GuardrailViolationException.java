package fr.simplex_software.workshop.security.springai;

public class GuardrailViolationException extends RuntimeException
{
  public GuardrailViolationException(String message)
  {
    super(message);
  }
}
