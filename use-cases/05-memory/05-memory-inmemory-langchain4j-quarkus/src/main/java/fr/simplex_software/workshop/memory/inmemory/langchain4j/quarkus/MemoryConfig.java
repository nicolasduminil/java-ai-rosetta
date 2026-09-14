package fr.simplex_software.workshop.memory.inmemory.langchain4j.quarkus;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;

/**
 * Builds the {@link Assistant} by hand with a {@code ChatMemoryProvider} having one
 * {@link MessageWindowChatMemory} per conversation id, kept in the default in-memory store. The
 * Ollama {@link ChatModel} is the one Quarkus auto-configures from {@code quarkus.langchain4j.ollama.*}.
 */
@ApplicationScoped
public class MemoryConfig
{
  private static final int MAX_MESSAGES = 20;

  @Produces
  @ApplicationScoped
  Assistant assistant(ChatModel chatModel)
  {
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
        .id(memoryId)
        .maxMessages(MAX_MESSAGES)
        .build())
      .build();
  }
}
