package fr.simplex_software.workshop.memory.persistent.springai;

import fr.simplex_software.workshop.memory.api.*;
import jakarta.validation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController
{
  private final ConversationService conversationService;

  public ChatController(ConversationService conversationService)
  {
    this.conversationService = conversationService;
  }

  @PostMapping(path = "/chat", produces = "application/json")
  public Answer chat(@Valid @RequestBody Question question)
  {
    return conversationService.chat(question);
  }
}
