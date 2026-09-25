package fr.simplex_software.workshop.ragmodular.langchain4j.boot;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.http.client.jdk.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.rag.*;
import dev.langchain4j.rag.content.aggregator.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.rag.query.router.*;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.qdrant.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * Builds the embedding and vector-store beans by hand. The chat {@link ChatModel} is autoconfigured by the
 * {@code langchain4j-ollama-spring-boot4-starter}; the embedding model, store and multi-source retriever are
 * wired here.
 */
@Configuration
public class RagConfig
{
  private static final Logger LOG = LoggerFactory.getLogger(RagConfig.class);
  private static final int MAX_RESULTS = 3;

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

  @Bean
  Assistant assistant(ChatModel chatModel, EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore)
  {
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .retrievalAugmentor(modularAugmentor(embeddingModel, embeddingStore))
      .build();
  }

  private static RetrievalAugmentor modularAugmentor(EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore)
  {
    return DefaultRetrievalAugmentor.builder()
      .queryRouter(new DefaultQueryRouter(
        sourceRetriever(embeddingModel, embeddingStore, "briefing"),
        sourceRetriever(embeddingModel, embeddingStore, "press")))
      .contentAggregator(new DefaultContentAggregator())
      .build();
  }

  private static ContentRetriever sourceRetriever(EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore, String source)
  {
    return EmbeddingStoreContentRetriever.builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .maxResults(MAX_RESULTS)
      .filter(metadataKey("source").isEqualTo(source))
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
