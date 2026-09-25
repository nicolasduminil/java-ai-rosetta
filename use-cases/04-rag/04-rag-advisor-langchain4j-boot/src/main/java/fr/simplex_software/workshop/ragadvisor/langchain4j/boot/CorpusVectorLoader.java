package fr.simplex_software.workshop.ragadvisor.langchain4j.boot;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.document.splitter.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.store.embedding.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

@Component
public class CorpusVectorLoader
{
  private static final Logger LOG = LoggerFactory.getLogger(CorpusVectorLoader.class);

  private final EmbeddingStoreIngestor ingestor;

  public CorpusVectorLoader(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore)
  {
    this.ingestor = EmbeddingStoreIngestor.builder()
      .documentSplitter(DocumentSplitters.recursive(300, 30))
      .embeddingModel(embeddingModel)
      .embeddingStore(embeddingStore)
      .build();
  }

  public void load(String text)
  {
    ingestor.ingest(Document.from(text));
    LOG.info("Ingested corpus ({} extracted characters)", text.length());
  }
}
