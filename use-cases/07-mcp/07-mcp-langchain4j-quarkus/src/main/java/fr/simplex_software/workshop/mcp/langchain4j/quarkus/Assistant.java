package fr.simplex_software.workshop.mcp.langchain4j.quarkus;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.*;
import io.quarkiverse.langchain4j.mcp.runtime.*;

/**
 * The declarative Quarkus AI service.
 */
@RegisterAiService
public interface Assistant
{
  @SystemMessage("You are a catalog assistant. Use the available tools to look up catalog items. Answer concisely.")
  @McpToolBox
  String ask(@UserMessage String question);
}
