package fr.simplex_software.workshop.security.springai;

import org.springframework.ai.document.*;
import org.springframework.ai.transformer.splitter.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Ingests a document tagged with its owner. The {@code owner} metadata rides through the splitter onto every
 * chunk and becomes the Qdrant payload field the retrieval filter keys on.
 */
@Component
public class CorpusVectorLoader
{
  private final VectorStore vectorStore;
  private final TokenTextSplitter splitter = TokenTextSplitter.builder().build();

  public CorpusVectorLoader(VectorStore vectorStore)
  {
    this.vectorStore = vectorStore;
  }

  public void load(String text, String owner)
  {
    vectorStore.add(splitter.apply(List.of(new Document(text, Map.of("owner", owner)))));
  }
}
