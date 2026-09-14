package fr.simplex_software.workshop.memory.persistent.springai;

import fr.simplex_software.workshop.memory.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.vectorstore.*;
import org.springframework.ai.chat.memory.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

/**
 * Persistent conversational memory the Spring AI way: a {@link VectorStoreChatMemoryAdvisor} stores each
 * turn as a document in the {@link VectorStore} (Qdrant) and, on the next call, retrieves the most
 * relevant earlier turns of the same conversation by similarity. The conversation is selected per call by
 * the {@link ChatMemory#CONVERSATION_ID} advisor parameter.
 */
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
