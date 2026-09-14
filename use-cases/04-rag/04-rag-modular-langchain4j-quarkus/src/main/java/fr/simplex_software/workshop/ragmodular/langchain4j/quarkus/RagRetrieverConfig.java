package fr.simplex_software.workshop.ragmodular.langchain4j.quarkus;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.rag.*;
import dev.langchain4j.rag.content.aggregator.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.rag.query.router.*;
import dev.langchain4j.store.embedding.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@ApplicationScoped
public class RagRetrieverConfig
{
  private static final int MAX_RESULTS = 3;

  @Produces
  @ApplicationScoped
  RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore,
    EmbeddingModel embeddingModel)
  {
    return DefaultRetrievalAugmentor.builder()
      .queryRouter(new DefaultQueryRouter(
        sourceRetriever(embeddingStore, embeddingModel, "briefing"),
        sourceRetriever(embeddingStore, embeddingModel, "press")))
      .contentAggregator(new DefaultContentAggregator())
      .build();
  }

  private static ContentRetriever sourceRetriever(EmbeddingStore<TextSegment> embeddingStore,
    EmbeddingModel embeddingModel, String source)
  {
    return EmbeddingStoreContentRetriever.builder()
      .embeddingStore(embeddingStore)
      .embeddingModel(embeddingModel)
      .maxResults(MAX_RESULTS)
      .filter(metadataKey("source").isEqualTo(source))
      .build();
  }
}
