package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.model.moderation.*;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.*;
import dev.langchain4j.store.embedding.filter.*;
import fr.simplex_software.workshop.security.api.*;
import org.springframework.beans.factory.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

import java.util.stream.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.*;

/**
 * Retrieval is filtered by the authenticated caller with a metadata filter on the {@code owner} field, so
 * the assistant only ever sees the caller's own documents plus the public ones. The retrieved context is
 * handed to the guardrailed, tool-equipped assistant.
 */
@Service
public class LangChain4jRagService implements QuestionAnsweringService
{
  private static final int MAX_RESULTS = 4;

  private final Assistant assistant;
  private final EmbeddingModel embeddingModel;
  private final EmbeddingStore<TextSegment> embeddingStore;
  private final ObjectProvider<ModerationModel> moderationModels;

  public LangChain4jRagService(Assistant assistant, EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore, ObjectProvider<ModerationModel> moderationModels)
  {
    this.assistant = assistant;
    this.embeddingModel = embeddingModel;
    this.embeddingStore = embeddingStore;
    this.moderationModels = moderationModels;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    moderateIfEnabled(question.question());
    return new Answer(assistant.answer(retrieveContext(question.question(),
      currentUser()), question.question()));
  }

  private void moderateIfEnabled(String text)
  {
    ModerationModel moderationModel = moderationModels.getIfAvailable();
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
      .build())
      .matches().stream()
      .map(match ->
        match.embedded().text()).collect(Collectors.joining("\n\n"));
  }

  private String currentUser()
  {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "anonymous";
  }
}
