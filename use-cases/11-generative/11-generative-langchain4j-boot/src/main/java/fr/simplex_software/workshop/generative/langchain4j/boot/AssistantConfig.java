package fr.simplex_software.workshop.generative.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.service.*;
import org.springframework.context.annotation.*;

@Configuration
public class AssistantConfig
{
  @Bean
  TextAssistant textAssistant(ChatModel chatModel)
  {
    return AiServices.builder(TextAssistant.class).chatModel(chatModel).build();
  }
}
