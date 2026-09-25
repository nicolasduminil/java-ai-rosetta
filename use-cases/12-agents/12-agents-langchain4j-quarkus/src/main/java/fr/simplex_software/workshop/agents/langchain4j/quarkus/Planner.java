package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import dev.langchain4j.service.*;
import io.quarkiverse.langchain4j.*;

@RegisterAiService(tools = Catalog.class)
public interface Planner
{
  @SystemMessage("You reach the user's goal by calling the available tools when needed, then give the answer.")
  @UserMessage("{goal}")
  String plan(@V("goal") String goal);
}
