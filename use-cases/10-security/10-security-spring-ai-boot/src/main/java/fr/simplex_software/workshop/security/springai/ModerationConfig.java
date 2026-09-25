package fr.simplex_software.workshop.security.springai;

import com.openai.client.*;
import com.openai.client.okhttp.*;
import org.springframework.ai.moderation.*;
import org.springframework.ai.openai.*;
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
    OpenAIClient openAiClient = OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    return OpenAiModerationModel.builder().openAiClient(openAiClient).build();
  }
}
