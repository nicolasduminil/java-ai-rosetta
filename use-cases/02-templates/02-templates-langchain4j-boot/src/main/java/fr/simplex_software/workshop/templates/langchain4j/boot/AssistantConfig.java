package fr.simplex_software.workshop.templates.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link Assistant}. The {@link ChatModel} is autoconfigured by the
 * {@code langchain4j-ollama-spring-boot4-starter} from {@code langchain4j.ollama.chat-model.*}, including
 * the {@code RESPONSE_FORMAT_JSON_SCHEMA} capability this use case needs for structured output.
 */
@Configuration
public class AssistantConfig
{
  @Bean
  Assistant assistant(ChatModel chatModel)
  {
    return AiServices.builder(Assistant.class).chatModel(chatModel).build();
  }
}
