package fr.simplex_software.workshop.security.springai;

/**
 * Raised by the guardrail advisor when input looks like a prompt injection or output would leak forbidden
 * content. The controller advice turns it into HTTP 403.
 */
public class GuardrailViolationException extends RuntimeException
{
  public GuardrailViolationException(String message)
  {
    super(message);
  }
}
