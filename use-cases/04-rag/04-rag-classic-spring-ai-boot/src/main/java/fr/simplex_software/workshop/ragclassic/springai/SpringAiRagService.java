package fr.simplex_software.workshop.ragclassic.springai;

import fr.simplex_software.workshop.rag.api.*;
import org.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.document.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.util.stream.*;

@Service
public class SpringAiRagService implements QuestionAnsweringService
{
  private static final Logger LOG = LoggerFactory.getLogger(SpringAiRagService.class);
  private static final int TOP_K = 4;

  private final ChatClient chatClient;
  private final VectorStore vectorStore;

  @Value("classpath:/prompts/ragSystemPrompt.st")
  private Resource systemPromptTemplate;

  public SpringAiRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore)
  {
    this.chatClient = chatClientBuilder.build();
    this.vectorStore = vectorStore;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(chatClient.prompt()
      .system(system -> system.text(systemPromptTemplate).param("context", retrieveContext(question.question())))
      .user(question.question())
      .call()
      .content());
  }

  private String retrieveContext(String query)
  {
    var hits = vectorStore.similaritySearch(
      SearchRequest.builder().query(query).topK(TOP_K).build());
    LOG.info("Retrieved {} chunk(s) for query \"{}\"", hits.size(), query);
    return hits.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
  }
}
