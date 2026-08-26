package fr.simplex_software.workshop.templates.api;

import java.io.*;
import java.nio.charset.*;

/**
 * Loads the reference document for a topic from the classpath, or returns an empty string when the
 * application carries none for it.
 *
 * <p>Deliberately framework-neutral - plain classpath I/O, no Spring, no CDI, no AI. Living in the
 * common module, it guarantees every stack answers from the identical corpus, which is what keeps
 * the "same behaviour, three stacks" claim honest. It is stateless, so each stack constructs it
 * directly rather than wiring it through its container.
 */
public final class ReferenceService
{
  private static final String LOCATION = "/reference/%s.txt";

  /**
   * @param topic the subject asked about; mapped to {@code /reference/<slug>.txt} where the slug is
   *              the lower-cased topic with spaces turned into underscores
   * @return the reference text, or {@code ""} when there is no document for this topic
   */
  public String referenceFor(String topic)
  {
    var location = LOCATION.formatted(topic.toLowerCase().replace(' ', '_'));
    try (var in = getClass().getResourceAsStream(location))
    {
      if (in == null)
        return "";
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException e)
    {
      return "";
    }
  }
}
