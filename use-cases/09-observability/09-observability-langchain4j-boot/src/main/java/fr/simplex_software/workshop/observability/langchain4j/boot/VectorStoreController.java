package fr.simplex_software.workshop.observability.langchain4j.boot;

import fr.simplex_software.workshop.observability.api.*;
import io.qdrant.client.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vectorstore")
public class VectorStoreController
{
  private final QdrantClient qdrantClient;
  private final String collectionName;

  public VectorStoreController(QdrantClient qdrantClient,
    @Value("${langchain4j.qdrant.collection-name}") String collectionName)
  {
    this.qdrantClient = qdrantClient;
    this.collectionName = collectionName;
  }

  @GetMapping("/count")
  public VectorStoreCount count() throws Exception
  {
    return new VectorStoreCount(qdrantClient.countAsync(collectionName).get());
  }
}
