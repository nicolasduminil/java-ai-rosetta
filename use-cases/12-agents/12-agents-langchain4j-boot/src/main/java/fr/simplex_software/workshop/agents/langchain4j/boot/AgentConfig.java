package fr.simplex_software.workshop.agents.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

/**
 * Builds the declarative {@link Assistant} and the tool-calling {@link Planner}. The {@link ChatModel} is
 * autoconfigured by the {@code langchain4j-ollama-spring-boot4-starter} from
 * {@code langchain4j.ollama.chat-model.*}.
 */
@Configuration
public class AgentConfig
{
  @Bean
  Assistant assistant(ChatModel chatModel)
  {
    return AiServices.builder(Assistant.class).chatModel(chatModel).build();
  }

  @Bean
  Planner planner(ChatModel chatModel, Catalog catalog)
  {
    return AiServices.builder(Planner.class).chatModel(chatModel).tools(catalog).build();
  }
}
