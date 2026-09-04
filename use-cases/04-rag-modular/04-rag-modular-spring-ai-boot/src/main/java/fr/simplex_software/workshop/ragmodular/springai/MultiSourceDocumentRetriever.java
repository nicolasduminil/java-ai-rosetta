package fr.simplex_software.workshop.ragmodular.springai;

import org.springframework.ai.document.*;
import org.springframework.ai.rag.*;
import org.springframework.ai.rag.retrieval.search.*;

import java.util.*;

public class MultiSourceDocumentRetriever implements DocumentRetriever
{
  private final List<DocumentRetriever> delegates;

  public MultiSourceDocumentRetriever(DocumentRetriever... delegates)
  {
    this.delegates = List.of(delegates);
  }

  @Override
  public List<Document> retrieve(Query query)
  {
    var byId = new LinkedHashMap<String, Document>();
    delegates.forEach(delegate ->
      delegate.retrieve(query).forEach(document -> byId.putIfAbsent(document.getId(), document)));
    return List.copyOf(byId.values());
  }
}
