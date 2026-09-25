package fr.simplex_software.workshop.generative.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link TextAssistant}. The {@link ChatModel} is autoconfigured by the
 * {@code langchain4j-ollama-spring-boot4-starter} from {@code langchain4j.ollama.chat-model.*}.
 */
@Configuration
public class AssistantConfig
{
  @Bean
  TextAssistant textAssistant(ChatModel chatModel)
  {
    return AiServices.builder(TextAssistant.class).chatModel(chatModel).build();
  }
}
