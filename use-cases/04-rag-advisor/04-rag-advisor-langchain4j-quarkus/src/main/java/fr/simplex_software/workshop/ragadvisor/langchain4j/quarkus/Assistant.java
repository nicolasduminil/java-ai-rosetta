package fr.simplex_software.workshop.ragadvisor.langchain4j.quarkus;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.*;

@RegisterAiService
interface Assistant
{
  @SystemMessage("""
    You are a helpful assistant. Answer the user's question using ONLY the provided context.
    If the answer is not contained in the context, say that you don't know.
    Answer concisely.""")
  String answer(String question);
}
