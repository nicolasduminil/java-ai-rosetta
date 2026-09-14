package fr.simplex_software.workshop.ragadvisor.langchain4j.quarkus;

import fr.simplex_software.workshop.rag.api.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;

/**
 * Delegates to the {@link Assistant} AI Service, whose {@code ContentRetriever} performs
 * retrieval and context injection declaratively.
 */
@ApplicationScoped
public class QuarkusRagService implements QuestionAnsweringService
{
  private final Assistant assistant;

  @Inject
  public QuarkusRagService(Assistant assistant)
  {
    this.assistant = assistant;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(assistant.answer(question.question()));
  }
}
