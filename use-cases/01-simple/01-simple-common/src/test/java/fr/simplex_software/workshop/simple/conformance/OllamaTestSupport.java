package fr.simplex_software.workshop.simple.conformance;

import org.testcontainers.ollama.*;
import org.testcontainers.utility.*;

/**
 * This single Ollama container is used by all the tests.
 *
 * <p>The container is started lazily on first access and shared for the lifetime of the JVM.
 * So only one pull operation is done, whatever the number of the test classes is.
 */
public final class OllamaTestSupport
{
  private static final String IMAGE = System.getProperty("ollama.image", "ollama/ollama:latest");
  private static final String MODEL = System.getProperty("ollama.model", "qwen2.5:0.5b");

  private static final OllamaContainer OLLAMA = startWithModel();

  private OllamaTestSupport()
  {
  }

  public static String baseUrl()
  {
    return OLLAMA.getEndpoint();
  }

  public static String model()
  {
    return MODEL;
  }

  private static OllamaContainer startWithModel()
  {
    var ollama = new OllamaContainer(DockerImageName.parse(IMAGE));
    ollama.start();
    try
    {
      var pull = ollama.execInContainer("ollama", "pull", MODEL);
      if (pull.getExitCode() != 0)
        throw new IllegalStateException("Pulling model " + MODEL + " failed: " + pull.getStderr());
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not pull model " + MODEL, e);
    }
    return ollama;
  }
}
