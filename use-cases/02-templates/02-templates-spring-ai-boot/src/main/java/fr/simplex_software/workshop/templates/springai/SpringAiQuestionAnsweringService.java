package fr.simplex_software.workshop.templates.springai;

import fr.simplex_software.workshop.templates.api.*;
import org.slf4j.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.metadata.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiQuestionAnsweringService implements QuestionAnsweringService
{
  private static final Logger LOG = LoggerFactory.getLogger(SpringAiQuestionAnsweringService.class);

  private final ChatClient chatClient;
  private final ReferenceService referenceService = new ReferenceService();

  @Value("classpath:/prompts/systemPromptTemplate.st")
  private Resource systemPromptTemplate;

  public SpringAiQuestionAnsweringService(ChatClient.Builder chatClientBuilder)
  {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Answer askQuestion(Question question)
  {
    var responseEntity = chatClient.prompt()
      .system(system -> system
        .text(systemPromptTemplate)
        .param("topic", question.topic())
        .param("reference", referenceService.referenceFor(question.topic())))
      .user(question.question())
      .call()
      .responseEntity(Answer.class);

    logUsage(responseEntity.response().getMetadata().getUsage());
    return responseEntity.entity();
  }

  private void logUsage(Usage usage)
  {
    LOG.info("Token usage: prompt={}, generation={}, total={}",
      usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
  }
}
