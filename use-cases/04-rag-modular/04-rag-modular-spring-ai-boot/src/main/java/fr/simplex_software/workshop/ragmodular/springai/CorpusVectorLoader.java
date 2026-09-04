package fr.simplex_software.workshop.ragmodular.springai;

import org.slf4j.*;
import org.springframework.ai.document.*;
import org.springframework.ai.transformer.splitter.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Splits the Tika-extracted text into chunks and stores them, tagging every chunk with its {@code source}.
 */
@Component
public class CorpusVectorLoader
{
  private static final Logger LOG = LoggerFactory.getLogger(CorpusVectorLoader.class);

  private final VectorStore vectorStore;
  private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

  public CorpusVectorLoader(VectorStore vectorStore)
  {
    this.vectorStore = vectorStore;
  }

  public void load(String text, String source)
  {
    var chunks = splitter.apply(List.of(new Document(text, Map.of("source", source))));
    vectorStore.add(chunks);
    LOG.info("Ingested {} chunk(s) of source '{}' from {} extracted characters", chunks.size(), source, text.length());
  }
}
