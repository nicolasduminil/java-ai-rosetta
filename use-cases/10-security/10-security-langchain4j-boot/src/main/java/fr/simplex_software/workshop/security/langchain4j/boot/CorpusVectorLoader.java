package fr.simplex_software.workshop.security.langchain4j.boot;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.document.splitter.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.store.embedding.*;
import org.springframework.stereotype.*;

@Component
public class CorpusVectorLoader
{
  private final EmbeddingStoreIngestor ingestor;

  public CorpusVectorLoader(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore)
  {
    this.ingestor = EmbeddingStoreIngestor.builder()
      .documentSplitter(DocumentSplitters.recursive(300, 30))
      .embeddingModel(embeddingModel)
      .embeddingStore(embeddingStore)
      .build();
  }

  public void load(String text, String owner)
  {
    ingestor.ingest(Document.from(text, Metadata.metadata("owner", owner)));
  }
}
