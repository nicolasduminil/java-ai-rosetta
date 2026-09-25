package fr.simplex_software.workshop.memory.persistent.langchain4j.quarkus;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import io.qdrant.client.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import org.eclipse.microprofile.config.inject.*;

@ApplicationScoped
public class MemoryConfig
{
  private static final int MAX_MESSAGES = 20;
  private static final String COLLECTION_NAME = "chat-memory";

  @Produces
  @ApplicationScoped
  Assistant assistant(ChatModel chatModel,
    @ConfigProperty(name = "chat-memory.qdrant.host") String host,
    @ConfigProperty(name = "chat-memory.qdrant.port") int port)
  {
    var qdrantClient = new QdrantClient(QdrantGrpcClient.newBuilder(host, port, false).build());
    var chatMemoryStore = new QdrantChatMemoryStore(qdrantClient, COLLECTION_NAME);
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
