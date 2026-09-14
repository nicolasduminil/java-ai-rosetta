package fr.simplex_software.workshop.memory.persistent.langchain4j.boot;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import dev.langchain4j.store.memory.chat.*;
import io.qdrant.client.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Wires the {@link ChatModel}, the {@link QdrantClient}, the persistent {@link QdrantChatMemoryStore},
 * and the {@link Assistant} whose {@code ChatMemoryProvider} keeps one window per conversation id in that
 * Qdrant-backed store.
 */
@Configuration
public class MemoryConfig
{
  private static final int MAX_MESSAGES = 20;

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
  ChatMemoryStore chatMemoryStore(QdrantClient qdrantClient,
    @Value("${langchain4j.qdrant.collection-name}") String collectionName)
  {
    return new QdrantChatMemoryStore(qdrantClient, collectionName);
  }

  @Bean
  Assistant assistant(ChatModel chatModel, ChatMemoryStore chatMemoryStore)
  {
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
        .id(memoryId)
        .maxMessages(MAX_MESSAGES)
        .chatMemoryStore(chatMemoryStore)
        .build())
      .build();
  }
}
