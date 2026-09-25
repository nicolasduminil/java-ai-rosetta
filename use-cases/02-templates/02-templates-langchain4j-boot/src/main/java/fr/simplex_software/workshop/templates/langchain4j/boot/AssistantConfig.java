package fr.simplex_software.workshop.templates.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

@Configuration
public class AssistantConfig
{
  @Bean
  Assistant assistant(ChatModel chatModel)
  {
    return AiServices.builder(Assistant.class).chatModel(chatModel).build();
  }
}
