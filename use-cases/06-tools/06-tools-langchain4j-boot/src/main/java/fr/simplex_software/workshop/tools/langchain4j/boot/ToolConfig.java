package fr.simplex_software.workshop.tools.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

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
