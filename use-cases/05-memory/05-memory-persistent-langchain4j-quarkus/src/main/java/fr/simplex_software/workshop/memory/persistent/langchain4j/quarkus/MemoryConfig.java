package fr.simplex_software.workshop.memory.persistent.langchain4j.quarkus;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import io.qdrant.client.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;
import org.eclipse.microprofile.config.inject.*;

/**
 * Builds the {@link Assistant} by hand with a {@code ChatMemoryProvider} whose memory windows persist to
 * Qdrant through the hand-rolled {@link QdrantChatMemoryStore}. The Ollama {@link ChatModel} is the one
 * Quarkus auto-configures; the {@code QdrantClient} is built here from the Qdrant host and port.
 * <p>
 * Why hand-built, and not a {@code @RegisterAiService} interface with a custom {@code ChatMemoryStore}
 * bean (as the in-memory sibling uses auto-memory)? On Quarkus, auto-memory is tied to the AI service's
 * CDI scope: the extension clears the service's chat memory when the bean is destroyed. That is the right
 * default for a transient in-memory window, but against a durable Qdrant store it would erase the persisted
 * conversation. Hand-building the AI service decouples the memory from the CDI bean lifecycle, so the
 * durable rows outlive any bean.
 */
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
