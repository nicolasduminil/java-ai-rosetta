package fr.simplex_software.workshop.tools.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

/**
 * Builds the {@link Assistant} with the {@link CatalogTool} registered via {@code AiServices.tools(...)}.
 * The {@link ChatModel} is autoconfigured by the {@code langchain4j-ollama-spring-boot4-starter}.
 * The AI service runs the tool-calling loop. When the model requests the tool,
 * LangChain4j invokes the matching {@code @Tool} method and feeds the result back.
 */
@Configuration
public class ToolConfig
{
  @Bean
  Assistant assistant(ChatModel chatModel, CatalogTool catalogTool)
  {
    return AiServices.builder(Assistant.class)
      .chatModel(chatModel)
      .tools(catalogTool)
      .build();
  }
}
