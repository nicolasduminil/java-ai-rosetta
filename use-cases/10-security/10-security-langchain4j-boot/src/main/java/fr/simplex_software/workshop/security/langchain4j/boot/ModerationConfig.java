package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.model.moderation.*;
import dev.langchain4j.model.openai.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

/**
 * Registers LangChain4j's OpenAI moderation model, but only when an {@code OPENAI_API_KEY} is present.
 * Without a key the bean is absent and the assistant skips moderation, so the stack stays local and offline.
 */
@Configuration
public class ModerationConfig
{
  @Bean
  @ConditionalOnExpression("'${OPENAI_API_KEY:}'.length() > 0")
  ModerationModel moderationModel(@Value("${OPENAI_API_KEY}") String apiKey)
  {
    return OpenAiModerationModel.builder()
      .apiKey(apiKey)
      .modelName("omni-moderation-latest")
      .build();
  }
}
