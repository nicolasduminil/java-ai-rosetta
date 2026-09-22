package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.service.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.qdrant.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Wires the Qdrant vector store and builds the assistant. The guardrails and the tool are attached to the
 * AI service programmatically, which is where LangChain4j differs sharply from Spring AI: input and output
 * guardrails are a first-class part of the service, not something hand-written around the call.
 */
@Configuration
public class RagConfig
{
  private static final Logger LOG = LoggerFactory.getLogger(RagConfig.class);

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
  Assistant assistant(ChatModel chatModel, AuditLogTool auditLogTool,
    InjectionGuardrail injectionGuardrail, LeakGuardrail leakGuardrail)
  {
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .tools(auditLogTool)
      // By default LangChain4j compensates a tool error (feeds it back to the model, so a denied call still
      // answers 200). Rethrow it instead so the access-denied exception propagates to become HTTP 403.
      .toolExecutionErrorHandler((error, context) ->
      {
        if (error instanceof RuntimeException runtimeException)
          throw runtimeException;
        throw new RuntimeException(error);
      })
      .inputGuardrails(injectionGuardrail)
      .outputGuardrails(leakGuardrail)
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
