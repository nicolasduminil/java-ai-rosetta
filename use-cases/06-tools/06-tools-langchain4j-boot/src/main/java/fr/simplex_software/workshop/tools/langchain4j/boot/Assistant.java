package fr.simplex_software.workshop.tools.langchain4j.boot;

import dev.langchain4j.service.*;

interface Assistant
{
  @SystemMessage("You are a catalog assistant. Use the available tools to look up catalog items. Answer concisely.")
  String ask(@UserMessage String question);
}
