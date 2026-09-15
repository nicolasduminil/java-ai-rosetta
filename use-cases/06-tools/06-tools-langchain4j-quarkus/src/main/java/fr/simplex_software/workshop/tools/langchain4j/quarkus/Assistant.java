package fr.simplex_software.workshop.tools.langchain4j.quarkus;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.*;

/**
 * The declarative Quarkus AI service.
 */
@RegisterAiService(tools = CatalogTool.class)
public interface Assistant
{
  @SystemMessage("You are a catalog assistant. Use the available tools to look up catalog items. Answer concisely.")
  String ask(@UserMessage String question);
}
