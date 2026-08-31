package fr.simplex_software.workshop.templates.api;

import java.io.*;
import java.nio.charset.*;

public final class ReferenceService
{
  private static final String LOCATION = "/reference/%s.txt";

  public String referenceFor(String topic)
  {
    var location = LOCATION.formatted(topic.toLowerCase().replace(' ', '_'));
    try (var in = getClass().getResourceAsStream(location))
    {
      return in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException e)
    {
      throw new UncheckedIOException(e);
    }
  }
}
