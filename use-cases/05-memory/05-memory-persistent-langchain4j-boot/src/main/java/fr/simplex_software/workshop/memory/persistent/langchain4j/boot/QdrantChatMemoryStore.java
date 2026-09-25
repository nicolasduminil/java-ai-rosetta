package fr.simplex_software.workshop.memory.persistent.langchain4j.boot;

import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.*;
import io.qdrant.client.*;
import io.qdrant.client.grpc.Collections.*;
import io.qdrant.client.grpc.Points.*;

import java.nio.charset.*;
import java.util.*;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

public class QdrantChatMemoryStore implements ChatMemoryStore
{
  private static final String MESSAGES_KEY = "messages";

  private final QdrantClient client;
  private final String collectionName;

  public QdrantChatMemoryStore(QdrantClient client, String collectionName)
  {
    this.client = client;
    this.collectionName = collectionName;
    createCollectionIfMissing();
  }

  @Override
  public List<ChatMessage> getMessages(Object memoryId)
  {
    List<ChatMessage> messages = new ArrayList<>();
    try
    {
      var points = client.retrieveAsync(collectionName, id(pointId(memoryId)), true, false, null).get();
      if (!points.isEmpty())
      {
        var msgs = points.get(0).getPayloadMap().get(MESSAGES_KEY);
        if (msgs != null)
          messages = ChatMessageDeserializer.messagesFromJson(msgs.getStringValue());
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public void updateMessages(Object memoryId, List<ChatMessage> messages)
  {
    try
    {
      var point = PointStruct.newBuilder()
        .setId(id(pointId(memoryId)))
        .setVectors(vectors(1.0f))
        .putPayload(MESSAGES_KEY, value(ChatMessageSerializer.messagesToJson(messages)))
        .build();
      client.upsertAsync(collectionName, List.of(point)).get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteMessages(Object memoryId)
  {
    try
    {
      client.deleteAsync(collectionName, List.of(id(pointId(memoryId)))).get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private static UUID pointId(Object memoryId)
  {
    return UUID.nameUUIDFromBytes(memoryId.toString().getBytes(StandardCharsets.UTF_8));
  }

  private void createCollectionIfMissing()
  {
    try
    {
      if (!client.collectionExistsAsync(collectionName).get())
        client.createCollectionAsync(collectionName,
          VectorParams.newBuilder().setSize(1).setDistance(Distance.Cosine).build()).get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }
}
