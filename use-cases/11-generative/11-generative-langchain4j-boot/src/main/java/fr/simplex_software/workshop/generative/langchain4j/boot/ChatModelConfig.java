package fr.simplex_software.workshop.generative.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

import java.time.*;

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
      .timeout(Duration.ofSeconds(120))
      .build();
  }

  @Bean
  TextAssistant textAssistant(ChatModel chatModel)
  {
    return AiServices.builder(TextAssistant.class).chatModel(chatModel).build();
  }
}
