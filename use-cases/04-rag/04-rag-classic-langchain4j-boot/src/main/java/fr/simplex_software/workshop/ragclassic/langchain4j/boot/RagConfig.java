package fr.simplex_software.workshop.ragclassic.langchain4j.boot;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.http.client.jdk.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.qdrant.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Builds the embedding and vector-store beans by hand. The chat model is autoconfigured by the
 * {@code langchain4j-ollama-spring-boot4-starter}; only the embedding model and store are wired here.
 *
 * <p>Unlike Spring AI's Qdrant starter, {@link QdrantEmbeddingStore} does not create the collection,
 * so this stack pre-creates it from the embedding model's dimension.
 */
@Configuration
public class RagConfig
{
  private static final Logger LOG = LoggerFactory.getLogger(RagConfig.class);

  @Bean
  EmbeddingModel embeddingModel(
    @Value("${langchain4j.ollama.base-url}") String baseUrl,
    @Value("${langchain4j.ollama.embedding-model.model-name}") String modelName)
  {
    // The Boot-4 starter puts a second HTTP client (spring-restclient) on the classpath next to the
    // parent's jdk client, so this hand-wired model must pick one explicitly to avoid an SPI conflict.
    return OllamaEmbeddingModel.builder()
      .httpClientBuilder(new JdkHttpClientBuilder())
      .baseUrl(baseUrl).modelName(modelName).build();
  }

  @Bean
  QdrantClient qdrantClient(
    @Value("${langchain4j.qdrant.host}") String host,
    @Value("${langchain4j.qdrant.port}") int port)
  {
    return new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
  }

  @Bean
  EmbeddingStore<TextSegment> embeddingStore(
    QdrantClient qdrantClient,
    EmbeddingModel embeddingModel,
    @Value("${langchain4j.qdrant.collection-name}") String collectionName) throws Exception
  {
    createCollectionIfMissing(qdrantClient, collectionName, embeddingModel.dimension());
    return QdrantEmbeddingStore.builder()
      .client(qdrantClient)
      .collectionName(collectionName)
      .build();
  }

  private void createCollectionIfMissing(QdrantClient client, String collectionName, int dimension)
    throws Exception
  {
    if (!client.collectionExistsAsync(collectionName).get())
    {

      client.createCollectionAsync(collectionName,
        VectorParams.newBuilder().setSize(dimension).setDistance(Distance.Cosine).build()).get();
      LOG.info("Created Qdrant collection '{}' with dimension {}", collectionName, dimension);
    }
  }
}
