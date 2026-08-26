package fr.simplex_software.workshop.simple.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.ollama.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link ChatModel} by hand, because LangChain4j's Spring Boot starter
 * is compiled agaist classes that don't exist anymore in Spring Boot 4 and,
 * hence, it isn't supported by Spring Boot 4, as per the current release.
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
    var builder = OllamaChatModel.builder();
    builder.baseUrl(baseUrl).modelName(modelName).temperature(temperature);
    return builder.build();
  }
}
