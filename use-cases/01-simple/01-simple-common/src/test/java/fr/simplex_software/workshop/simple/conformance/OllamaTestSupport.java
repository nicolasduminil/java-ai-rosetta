package fr.simplex_software.workshop.simple.conformance;

import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The single Ollama container every conformance run talks to.
 *
 * <p>Started lazily on first access and shared for the lifetime of the JVM, so a module pays the
 * model pull once no matter how many test classes extend {@link AbstractQuestionAnsweringIT}.
 *
 * <p>Image and model come from system properties set by the failsafe plugin out of the root POM,
 * so all three stacks are provably tested against the same model. The defaults only matter when
 * a test is launched straight from an IDE.
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
