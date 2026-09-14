package fr.simplex_software.workshop.memory.inmemory.langchain4j.quarkus;

import fr.simplex_software.workshop.memory.api.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource
{
  private final ConversationService conversationService;

  public ChatResource(ConversationService conversationService)
  {
    this.conversationService = conversationService;
  }

  @POST
  public Answer chat(@Valid Question question)
  {
    return conversationService.chat(question);
  }
}
