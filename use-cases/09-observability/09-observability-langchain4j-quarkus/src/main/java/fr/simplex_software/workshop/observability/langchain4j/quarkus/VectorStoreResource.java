package fr.simplex_software.workshop.observability.langchain4j.quarkus;

import fr.simplex_software.workshop.observability.api.*;
import io.qdrant.client.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.*;

@Path("/vectorstore")
@Produces(MediaType.APPLICATION_JSON)
public class VectorStoreResource
{
  private final QdrantClient qdrantClient;
  private final String collectionName;

  public VectorStoreResource(QdrantClient qdrantClient,
    @ConfigProperty(name = "quarkus.langchain4j.qdrant.collection.name") String collectionName)
  {
    this.qdrantClient = qdrantClient;
    this.collectionName = collectionName;
  }

  @GET
  @Path("/count")
  public VectorStoreCount count() throws Exception
  {
    return new VectorStoreCount(qdrantClient.countAsync(collectionName).get());
  }
}
