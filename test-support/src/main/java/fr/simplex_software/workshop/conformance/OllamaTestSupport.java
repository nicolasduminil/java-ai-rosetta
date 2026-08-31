package fr.simplex_software.workshop.conformance;

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
  private static final String JUDGE_MODEL = System.getProperty("ollama.judge.model", "qwen2.5:3b");
  private static final String EMBEDDING_MODEL =
    System.getProperty("ollama.embedding.model", "nomic-embed-text");

  private static final OllamaContainer OLLAMA = startWithModel();

  private static boolean judgePulled;
  private static boolean embeddingPulled;

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

  /**
   * The LLM-as-judge model, pulled on first access so only evaluation tests pay for it. Judging is
   * harder than answering, so this is a larger model than {@link #model()} by default.
   */
  public static synchronized String judgeModel()
  {
    if (!judgePulled)
    {
      pull(JUDGE_MODEL);
      judgePulled = true;
    }
    return JUDGE_MODEL;
  }

  /**
   * The embedding model, pulled on first access so only retrieval-augmented use cases pay for it.
   * Answering models (see {@link #model()}) cannot embed, so RAG needs this dedicated model.
   */
  public static synchronized String embeddingModel()
  {
    if (!embeddingPulled)
    {
      pull(EMBEDDING_MODEL);
      embeddingPulled = true;
    }
    return EMBEDDING_MODEL;
  }

  private static OllamaContainer startWithModel()
  {
    var ollama = new OllamaContainer(DockerImageName.parse(IMAGE));
    ollama.start();
    pull(ollama, MODEL);
    return ollama;
  }

  private static void pull(String model)
  {
    pull(OLLAMA, model);
  }

  private static void pull(OllamaContainer ollama, String model)
  {
    try
    {
      var pull = ollama.execInContainer("ollama", "pull", model);
      if (pull.getExitCode() != 0)
        throw new IllegalStateException("Pulling model " + model + " failed: " + pull.getStderr());
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not pull model " + model, e);
    }
  }
}
