package fr.simplex_software.workshop.ragmodular.springai;

import fr.simplex_software.workshop.ragmodular.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.rag.advisor.*;
import org.springframework.ai.rag.retrieval.search.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.vectorstore.filter.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiRagService implements QuestionAnsweringService
{
  private static final int TOP_K = 3;

  private final ChatClient chatClient;

  public SpringAiRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore)
  {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
        .documentRetriever(new MultiSourceDocumentRetriever(
          sourceRetriever(vectorStore, "briefing"),
          sourceRetriever(vectorStore, "press")))
        .build())
      .build();
  }

  private static VectorStoreDocumentRetriever sourceRetriever(VectorStore vectorStore, String source)
  {
    return VectorStoreDocumentRetriever.builder()
      .vectorStore(vectorStore)
      .topK(TOP_K)
      .filterExpression(new FilterExpressionBuilder().eq("source", source).build())
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
