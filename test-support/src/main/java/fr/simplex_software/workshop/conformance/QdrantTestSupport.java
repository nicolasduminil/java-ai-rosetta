package fr.simplex_software.workshop.conformance;

import org.testcontainers.qdrant.*;
import org.testcontainers.utility.*;

/**
 * A single Qdrant vector-store container shared by all retrieval-augmented tests.
 *
 * <p>Like {@link OllamaTestSupport} the container is started lazily on first access and kept for the
 * lifetime of the JVM, so every test class in a module reuses the same store. The client transport is
 * gRPC, so callers wire {@link #host()} together with {@link #grpcPort()}.
 */
public final class QdrantTestSupport
{
  private static final String IMAGE = System.getProperty("qdrant.image", "qdrant/qdrant:v1.17.0");

  private static final QdrantContainer QDRANT = start();

  private QdrantTestSupport()
  {
  }

  public static String host()
  {
    return QDRANT.getHost();
  }

  public static int grpcPort()
  {
    return QDRANT.getGrpcPort();
  }

  private static QdrantContainer start()
  {
    var qdrant = new QdrantContainer(DockerImageName.parse(IMAGE));
    qdrant.start();
    return qdrant;
  }
}
