package fr.simplex_software.workshop.ragmodular.langchain4j.boot;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.document.splitter.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.store.embedding.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

/**
 * Splits the Tika-extracted text and ingests it, tagging every segment with its {@code source} so the
 * modular pipeline's per-source retrievers can query each source independently.
 */
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

  public void load(String text, String source)
  {
    ingestor.ingest(Document.from(text, Metadata.from("source", source)));
    LOG.info("Ingested corpus source '{}' ({} extracted characters)", source, text.length());
  }
}
