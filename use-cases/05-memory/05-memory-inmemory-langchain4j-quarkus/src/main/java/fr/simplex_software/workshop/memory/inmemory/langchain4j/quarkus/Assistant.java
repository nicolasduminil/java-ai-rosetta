package fr.simplex_software.workshop.memory.inmemory.langchain4j.quarkus;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.*;
import jakarta.enterprise.context.*;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
@RegisterAiService
public interface Assistant
{
  @SystemMessage("You are a helpful assistant. Answer concisely.")
  String chat(@MemoryId String conversationId, @UserMessage String question);
}
