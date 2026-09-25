package fr.simplex_software.workshop.memory.inmemory.langchain4j.boot;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

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
