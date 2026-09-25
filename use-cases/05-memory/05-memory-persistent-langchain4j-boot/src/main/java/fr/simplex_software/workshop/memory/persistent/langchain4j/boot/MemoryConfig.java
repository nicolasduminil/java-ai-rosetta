package fr.simplex_software.workshop.memory.persistent.langchain4j.boot;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import dev.langchain4j.store.memory.chat.*;
import io.qdrant.client.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class MemoryConfig
{
  private static final int MAX_MESSAGES = 20;

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
