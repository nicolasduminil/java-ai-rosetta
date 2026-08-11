package fr.simplex_software.workshop.simple.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.ollama.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link ChatModel} by hand, because LangChain4j's Spring Boot starter does not run on
 * Spring Boot 4 - it drags in a Spring HTTP client compiled against a class Boot 4 removed.
 *
 * <p>This class is the price of that gap, and the clearest single difference between the two
 * Spring Boot tracks: the Spring AI module has no equivalent, because its starter auto-configures
 * the model from properties alone.
 *
 * <p>The property names deliberately match the ones the starter would use, so adopting it later
 * means deleting this class and changing nothing else.
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
    // Bound to a variable rather than chained: the setters return the base builder type, whose
    // build() yields OllamaBaseChatModel instead of OllamaChatModel.
    var builder = OllamaChatModel.builder();
    builder.baseUrl(baseUrl).modelName(modelName).temperature(temperature);
    return builder.build();
  }
}
