package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.moderation.*;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.*;
import fr.simplex_software.workshop.security.api.*;
import io.quarkiverse.langchain4j.jaxrsclient.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import org.eclipse.microprofile.config.inject.*;
import org.eclipse.microprofile.jwt.*;

import java.util.*;
import java.util.stream.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.*;

@ApplicationScoped
public class QuarkusRagService implements QuestionAnsweringService
{
  private static final int MAX_RESULTS = 4;

  private final Assistant assistant;
  private final EmbeddingModel embeddingModel;
  private final EmbeddingStore<TextSegment> embeddingStore;
  private final JsonWebToken jwt;
  private final ModerationModel moderationModel;

  @Inject
  public QuarkusRagService(Assistant assistant, EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore, JsonWebToken jwt,
    @ConfigProperty(name = "openai.api-key") Optional<String> openAiApiKey)
  {
    this.assistant = assistant;
    this.embeddingModel = embeddingModel;
    this.embeddingStore = embeddingStore;
    this.jwt = jwt;
    // The HTTP client is chosen explicitly: both the JDK and the Quarkus JAX-RS langchain4j client
    // factories are on the classpath, so an unspecified builder fails with "multiple HTTP clients found".
    // The JAX-RS client is the Quarkus-managed one (Vert.x, native-image aware).
    this.moderationModel = openAiApiKey.filter(key -> !key.isBlank())
      .map(key -> (ModerationModel) OpenAiModerationModel.builder()
        .httpClientBuilder(new JaxRsHttpClientBuilder())
        .apiKey(key).modelName("omni-moderation-latest").build())
      .orElse(null);
  }

  @Override
  public Answer askQuestion(Question question)
  {
    moderateIfEnabled(question.question());
    return new Answer(assistant.answer(retrieveContext(question.question(),
      jwt.getName()), question.question()));
  }

  private void moderateIfEnabled(String text)
  {
    if (moderationModel != null)
    {
      Moderation moderation = moderationModel.moderate(text).content();
      if (moderation.flagged())
        throw new ModerationException("input flagged by content moderation", moderation);
    }
  }

  private String retrieveContext(String query, String user)
  {
    return embeddingStore.search(EmbeddingSearchRequest.builder()
      .queryEmbedding(embeddingModel.embed(query).content())
      .maxResults(MAX_RESULTS)
      .filter(metadataKey("owner").isEqualTo(user).or(metadataKey("owner").isEqualTo("public")))
      .build()).matches()
      .stream()
      .map(match ->
        match.embedded().text()).collect(Collectors.joining("\n\n"));
  }
}
