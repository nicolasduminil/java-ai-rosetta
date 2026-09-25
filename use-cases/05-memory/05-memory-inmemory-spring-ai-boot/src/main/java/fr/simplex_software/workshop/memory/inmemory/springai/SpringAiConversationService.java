package fr.simplex_software.workshop.memory.inmemory.springai;

import fr.simplex_software.workshop.memory.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.ai.chat.memory.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiConversationService implements ConversationService
{
  private static final int MAX_MESSAGES = 20;

  private final ChatClient chatClient;

  public SpringAiConversationService(ChatClient.Builder chatClientBuilder)
  {
    var chatMemory = MessageWindowChatMemory.builder()
      .chatMemoryRepository(new InMemoryChatMemoryRepository())
      .maxMessages(MAX_MESSAGES)
      .build();
    this.chatClient = chatClientBuilder
      .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
      .build();
  }

  @Override
  public Answer chat(Question question)
  {
    return new Answer(chatClient.prompt()
      .user(question.question())
      .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, question.conversationId()))
      .call()
      .content());
  }
}
