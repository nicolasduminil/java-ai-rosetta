package fr.simplex_software.workshop.observability.springai;

import fr.simplex_software.workshop.observability.api.*;
import io.qdrant.client.*;
import org.springframework.ai.document.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/vectorstore")
public class VectorStoreController
{
  private final VectorStore vectorStore;
  private final String collectionName;

  public VectorStoreController(VectorStore vectorStore,
    @Value("${spring.ai.vectorstore.qdrant.collection-name}") String collectionName)
  {
    this.vectorStore = vectorStore;
    this.collectionName = collectionName;
  }

  @GetMapping("/count")
  public VectorStoreCount count() throws Exception
  {
    QdrantClient client = vectorStore.<QdrantClient>getNativeClient()
      .orElseThrow(() -> new IllegalStateException("no native Qdrant client on the vector store"));
    return new VectorStoreCount(client.countAsync(collectionName).get());
  }

  @GetMapping("/search")
  public List<String> search(@RequestParam String query)
  {
    return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(4).build())
      .stream().map(Document::getText).toList();
  }
}
