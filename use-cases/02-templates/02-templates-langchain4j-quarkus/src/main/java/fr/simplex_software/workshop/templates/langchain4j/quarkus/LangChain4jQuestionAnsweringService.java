package fr.simplex_software.workshop.templates.langchain4j.quarkus;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.templates.api.*;
import jakarta.enterprise.context.*;
import org.slf4j.*;

@ApplicationScoped
public class LangChain4jQuestionAnsweringService implements QuestionAnsweringService
{
  private static final Logger LOG = LoggerFactory.getLogger(LangChain4jQuestionAnsweringService.class);

  private final Assistant assistant;
  private final ReferenceService referenceService = new ReferenceService();

  public LangChain4jQuestionAnsweringService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    var reference = referenceService.referenceFor(question.topic());
    Result<Answer> result = assistant.answer(question.topic(), reference, question.question());
    LOG.info("Token usage: {}", result.tokenUsage());
    return result.content();
  }
}
