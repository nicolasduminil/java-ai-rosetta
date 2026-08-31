package fr.simplex_software.workshop.ragclassic.langchain4j.boot;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.chat.*;
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
 * Builds the LangChain4j model, embedding and vector-store beans by hand (its Spring Boot starter is
 * incompatible with Spring Boot 4).
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
    return OllamaEmbeddingModel.builder().baseUrl(baseUrl).modelName(modelName).build();
  }

  @Bean
  ChatModel chatModel(
    @Value("${langchain4j.ollama.base-url}") String baseUrl,
    @Value("${langchain4j.ollama.chat-model.model-name}") String modelName,
    @Value("${langchain4j.ollama.chat-model.temperature}") Double temperature)
  {
    return OllamaChatModel.builder()
      .baseUrl(baseUrl)
      .modelName(modelName)
      .temperature(temperature)
      .build();
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
