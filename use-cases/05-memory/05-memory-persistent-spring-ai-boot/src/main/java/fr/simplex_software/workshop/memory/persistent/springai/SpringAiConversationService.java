package fr.simplex_software.workshop.memory.persistent.springai;

import fr.simplex_software.workshop.memory.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.vectorstore.*;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiConversationService implements ConversationService
{
  private static final int TOP_K = 10;

  private final ChatClient chatClient;

  public SpringAiConversationService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore)
  {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(VectorStoreChatMemoryAdvisor.builder(vectorStore).defaultTopK(TOP_K).build())
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
