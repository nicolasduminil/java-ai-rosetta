package fr.simplex_software.workshop.security.langchain4j.quarkus;

import io.qdrant.client.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import org.eclipse.microprofile.config.inject.*;

/**
 * Produces a Qdrant client for collection creation and ingestion.
 */
@ApplicationScoped
public class QdrantClientProducer
{
  @Produces
  @ApplicationScoped
  QdrantClient qdrantClient(
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.host") String host,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.port") int port)
  {
    return new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
  }

  void closeQdrantClient(@Disposes QdrantClient qdrantClient)
  {
    qdrantClient.close();
  }
}
