package fr.simplex_software.workshop.memory.inmemory.langchain4j.boot;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link Assistant} with a {@code ChatMemoryProvider} having one {@link MessageWindowChatMemory}
 * per conversation id, kept in memory (the default {@code InMemoryChatMemoryStore}). The {@link ChatModel} is
 * autoconfigured by the {@code langchain4j-ollama-spring-boot4-starter}.
 */
@Configuration
public class MemoryConfig
{
  private static final int MAX_MESSAGES = 20;

  @Bean
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
