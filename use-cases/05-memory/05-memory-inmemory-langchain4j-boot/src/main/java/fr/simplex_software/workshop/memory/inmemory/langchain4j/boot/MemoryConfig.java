package fr.simplex_software.workshop.memory.inmemory.langchain4j.boot;

import dev.langchain4j.memory.chat.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link ChatModel} by hand since LangChain4j's Spring Boot starter is incompatible with Spring
 * Boot 4. Builds also the {@link Assistant} with a {@code ChatMemoryProvider} having one {@link MessageWindowChatMemory}
 * per conversation id, kept in memory (the default {@code InMemoryChatMemoryStore}).
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
