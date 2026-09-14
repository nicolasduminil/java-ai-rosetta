package fr.simplex_software.workshop.memory.persistent.langchain4j.boot;

import dev.langchain4j.service.*;

interface Assistant
{
  @SystemMessage("You are a helpful assistant. Answer concisely.")
  String chat(@MemoryId String conversationId, @UserMessage String question);
}
