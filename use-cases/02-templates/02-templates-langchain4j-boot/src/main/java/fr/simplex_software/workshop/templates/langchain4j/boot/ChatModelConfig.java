package fr.simplex_software.workshop.templates.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.chat.request.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link ChatModel} and the {@link Assistant} by hand, because LangChain4j's Spring Boot
 * starter is compiled against classes that no longer exist in Spring Boot 4 (see use case 01).
 *
 * <p>{@code RESPONSE_FORMAT_JSON_SCHEMA} makes the Ollama model honour the JSON schema that
 * {@code AiServices} derives from {@link fr.simplex_software.workshop.templates.api.Answer}, so even a
 * tiny model returns grammar-constrained, parseable structured output.
 */
@Configuration
public class ChatModelConfig
{
  @Bean
  ChatModel ollamaChatModel(
    @Value("${langchain4j.ollama.chat-model.base-url}") String baseUrl,
    @Value("${langchain4j.ollama.chat-model.model-name}") String modelName,
    @Value("${langchain4j.ollama.chat-model.temperature}") Double temperature)
  {
    return OllamaChatModel.builder()
      .baseUrl(baseUrl)
      .modelName(modelName)
      .temperature(temperature)
      .responseFormat(ResponseFormat.JSON)
      .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
      .build();
  }

  @Bean
  Assistant assistant(ChatModel chatModel)
  {
    return AiServices.builder(Assistant.class).chatModel(chatModel).build();
  }
}
