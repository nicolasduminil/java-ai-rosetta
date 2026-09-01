package fr.simplex_software.workshop.ragadvisor.springai;

import fr.simplex_software.workshop.rag.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.vectorstore.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiRagService implements QuestionAnsweringService
{
  private static final int TOP_K = 4;

  private final ChatClient chatClient;

  public SpringAiRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore)
  {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
        .searchRequest(SearchRequest.builder().topK(TOP_K).build())
        .build())
      .build();
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(chatClient.prompt()
      .user(question.question())
      .call()
      .content());
  }
}
