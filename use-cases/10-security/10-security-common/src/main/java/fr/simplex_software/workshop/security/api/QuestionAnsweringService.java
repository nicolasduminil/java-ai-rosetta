package fr.simplex_software.workshop.security.api;

/**
 * The answering contract. The caller's identity is not part of the payload: it comes from the
 * authenticated security context (a JWT), so each stack reads the current user and the retrieval is
 * filtered to the documents that user owns plus the public ones.
 */
public interface QuestionAnsweringService
{
  Answer askQuestion(Question question);
}
