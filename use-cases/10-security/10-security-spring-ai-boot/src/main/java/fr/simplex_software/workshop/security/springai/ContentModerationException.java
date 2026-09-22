package fr.simplex_software.workshop.security.springai;

/**
 * Raised when the moderation model flags the user's input. The controller advice turns it into HTTP 403.
 */
public class ContentModerationException extends RuntimeException
{
  public ContentModerationException(String message)
  {
    super(message);
  }
}
