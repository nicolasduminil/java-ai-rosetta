package fr.simplex_software.workshop.ragclassic.springai;

import org.slf4j.*;
import org.springframework.ai.document.*;
import org.springframework.ai.transformer.splitter.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Split the Tika-extracted text into chunks and store them.
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

  public void load(String text)
  {
    var chunks = splitter.apply(List.of(new Document(text)));
    vectorStore.add(chunks);
    LOG.info("Ingested {} chunk(s) from {} extracted characters", chunks.size(), text.length());
  }
}
