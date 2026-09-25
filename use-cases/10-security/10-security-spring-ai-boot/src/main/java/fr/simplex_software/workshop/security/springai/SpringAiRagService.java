package fr.simplex_software.workshop.security.springai;

import fr.simplex_software.workshop.security.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.document.*;
import org.springframework.ai.moderation.*;
import org.springframework.ai.vectorstore.*;
import org.springframework.ai.vectorstore.filter.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.*;

import java.util.stream.*;

@Service
public class SpringAiRagService implements QuestionAnsweringService
{
  private static final int TOP_K = 4;

  private final ChatClient chatClient;
  private final VectorStore vectorStore;
  private final ObjectProvider<ModerationModel> moderationModels;

  @Value("classpath:/prompts/ragSystemPrompt.st")
  private Resource systemPromptTemplate;

  public SpringAiRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore,
                            SecurityGuardAdvisor securityGuardAdvisor, AuditLogTool auditLogTool,
                            ObjectProvider<ModerationModel> moderationModels)
  {
    this.chatClient = chatClientBuilder
      .defaultAdvisors(securityGuardAdvisor)
      .defaultTools(auditLogTool)
      .build();
    this.vectorStore = vectorStore;
    this.moderationModels = moderationModels;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    moderateIfEnabled(question.question());
    return new Answer(chatClient.prompt()
      .system(system -> system.text(systemPromptTemplate)
        .param("context", retrieveContext(question.question(), currentUser())))
      .user(question.question())
      .call()
      .content());
  }

  private String retrieveContext(String query, String user)
  {
    var builder = new FilterExpressionBuilder();
    return vectorStore.similaritySearch(
        SearchRequest.builder().query(query).topK(TOP_K)
          .filterExpression(builder.or(builder.eq("owner", user),
            builder.eq("owner", "public")).build()).build())
      .stream().map(Document::getText).collect(Collectors.joining("\n\n"));
  }

  private void moderateIfEnabled(String text)
  {
    ModerationModel moderationModel = moderationModels.getIfAvailable();
    if (moderationModel != null)
      if (moderationModel.call(new ModerationPrompt(text))
        .getResult().getOutput().getResults().stream().anyMatch(ModerationResult::isFlagged))
        throw new ContentModerationException("input flagged by content moderation");
  }

  private String currentUser()
  {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null ? authentication.getName() : "anonymous";
  }
}
