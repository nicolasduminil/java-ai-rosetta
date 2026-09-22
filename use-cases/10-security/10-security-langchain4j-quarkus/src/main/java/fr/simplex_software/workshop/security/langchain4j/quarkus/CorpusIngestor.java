package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.model.embedding.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import io.quarkus.runtime.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.event.*;
import jakarta.inject.*;
import org.eclipse.microprofile.config.inject.*;
import org.slf4j.*;

import java.io.*;
import java.nio.charset.*;
import java.util.List;

/**
 * On startup, create the collection if needed and ingest the three owner-tagged corpus files (the file name
 * is the owner).
 */
@ApplicationScoped
public class CorpusIngestor
{
  private static final Logger LOG = LoggerFactory.getLogger(CorpusIngestor.class);
  private static final List<String> OWNERS = List.of("public", "alice", "bob");

  private final QdrantClient qdrantClient;
  private final EmbeddingModel embeddingModel;
  private final CorpusVectorLoader loader;
  private final String collectionName;

  @Inject
  public CorpusIngestor(QdrantClient qdrantClient, EmbeddingModel embeddingModel, CorpusVectorLoader loader,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.collection.name") String collectionName)
  {
    this.qdrantClient = qdrantClient;
    this.embeddingModel = embeddingModel;
    this.loader = loader;
    this.collectionName = collectionName;
  }

  void onStart(@Observes StartupEvent event) throws Exception
  {
    createCollectionIfMissing();
    for (String owner : OWNERS)
      loader.load(readCorpus("corpus/" + owner + ".txt"), owner);
  }

  private void createCollectionIfMissing() throws Exception
  {
    if (!qdrantClient.collectionExistsAsync(collectionName).get())
    {
      var dimension = embeddingModel.dimension();
      qdrantClient.createCollectionAsync(collectionName,
        VectorParams.newBuilder().setSize(dimension).setDistance(Distance.Cosine).build()).get();
      LOG.info("Created Qdrant collection '{}' with dimension {}", collectionName, dimension);
    }
  }

  private String readCorpus(String resource) throws IOException
  {
    try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
    {
      if (in == null)
        throw new IllegalStateException("Corpus resource not found on the classpath: " + resource);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
