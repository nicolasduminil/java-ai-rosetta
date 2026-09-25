package fr.simplex_software.workshop.multimodal.langchain4j.boot;

import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.image.*;
import dev.langchain4j.model.openai.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

/**
 * Hand-wires the three OpenAI models LangChain4j offers for this use case. There is no speech-synthesis
 * model as LangChain4j doesn't support any.
 */
@Configuration
public class OpenAiModelsConfig
{
  static
  {
    // The langchain4j-boot parent carries the Ollama starter's spring-restclient alongside the jdk client.
    // These OpenAI models resolve the SPI ambiguity through the client-selection system property langchain4j
    // reads at model-build time (the transcription model's builder cannot pick a client on its own).
    System.setProperty("langchain4j.http.clientBuilderFactory",
      "dev.langchain4j.http.client.jdk.JdkHttpClientBuilderFactory");
  }

  @Bean
  ChatModel chatModel(@Value("${openai.api-key}") String apiKey)
  {
    return OpenAiChatModel.builder()
      .apiKey(apiKey)
      .modelName("gpt-4o-mini")
      .temperature(0.0d)
      .build();
  }

  @Bean
  OpenAiAudioTranscriptionModel transcriptionModel(@Value("${openai.api-key}") String apiKey)
  {
    return OpenAiAudioTranscriptionModel.builder()
      .apiKey(apiKey)
      .modelName("whisper-1")
      .build();
  }

  @Bean
  ImageModel imageModel(@Value("${openai.api-key}") String apiKey)
  {
    return OpenAiImageModel.builder()
      .apiKey(apiKey)
      .modelName("gpt-image-1-mini")
      .size("1024x1024")
      .quality("low")
      .build();
  }
}
