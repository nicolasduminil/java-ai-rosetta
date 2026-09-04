package fr.simplex_software.workshop.ragmodular.langchain4j.quarkus;

import dev.langchain4j.model.embedding.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import io.quarkus.runtime.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.event.*;
import jakarta.inject.*;
import org.apache.camel.ProducerTemplate;
import org.eclipse.microprofile.config.inject.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

@ApplicationScoped
public class CorpusIngestor
{
  private static final Logger LOG = LoggerFactory.getLogger(CorpusIngestor.class);
  private static final Map<String, String> CORPUS = Map.of(
    "corpus/aurora-briefing.pdf", "briefing",
    "corpus/aurora-press.pdf", "press");

  private final ProducerTemplate producerTemplate;
  private final EmbeddingModel embeddingModel;
  private final String host;
  private final int port;
  private final String collectionName;

  @Inject
  public CorpusIngestor(ProducerTemplate producerTemplate, EmbeddingModel embeddingModel,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.host") String host,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.port") int port,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.collection.name") String collectionName)
  {
    this.producerTemplate = producerTemplate;
    this.embeddingModel = embeddingModel;
    this.host = host;
    this.port = port;
    this.collectionName = collectionName;
  }

  void onStart(@Observes StartupEvent event) throws Exception
  {
    createCollectionIfMissing();
    CORPUS.forEach(this::ingest);
  }

  private void createCollectionIfMissing() throws Exception
  {
    try (var client = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build()))
    {
      if (!client.collectionExistsAsync(collectionName).get())
      {
        var dimension = embeddingModel.dimension();
        client.createCollectionAsync(collectionName,
          VectorParams.newBuilder().setSize(dimension).setDistance(Distance.Cosine).build()).get();
        LOG.info("Created Qdrant collection '{}' with dimension {}", collectionName, dimension);
      }
    }
  }

  private void ingest(String resource, String source)
  {
    try (var in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource))
    {
      if (in == null)
        throw new IllegalStateException("Corpus resource not found on the classpath: " + resource);
      producerTemplate.sendBodyAndHeader("direct:ingest", in.readAllBytes(), "source", source);
    }
    catch (IOException e)
    {
      throw new UncheckedIOException("Failed to read corpus resource: " + resource, e);
    }
  }
}
