package fr.simplex_software.workshop.ragadvisor.langchain4j.quarkus;

import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.rag.*;
import dev.langchain4j.rag.content.retriever.*;
import dev.langchain4j.store.embedding.*;
import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.*;

@ApplicationScoped
public class RagRetrieverConfig
{
  private static final int MAX_RESULTS = 4;

  @Produces
  @ApplicationScoped
  RetrievalAugmentor retrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore,
    EmbeddingModel embeddingModel)
  {
    return DefaultRetrievalAugmentor.builder()
      .contentRetriever(EmbeddingStoreContentRetriever.builder()
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .maxResults(MAX_RESULTS)
        .build())
      .build();
  }
}
