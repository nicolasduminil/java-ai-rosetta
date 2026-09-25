package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.http.client.jdk.*;
import dev.langchain4j.model.moderation.*;
import dev.langchain4j.model.openai.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

@Configuration
public class ModerationConfig
{
  @Bean
  @ConditionalOnExpression("'${OPENAI_API_KEY:}'.length() > 0")
  ModerationModel moderationModel(@Value("${OPENAI_API_KEY}") String apiKey)
  {
    return OpenAiModerationModel.builder()
      .httpClientBuilder(new JdkHttpClientBuilder())
      .apiKey(apiKey)
      .modelName("omni-moderation-latest")
      .build();
  }
}
