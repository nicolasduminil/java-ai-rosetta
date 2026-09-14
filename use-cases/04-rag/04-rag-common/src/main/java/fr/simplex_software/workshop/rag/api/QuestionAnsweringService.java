package fr.simplex_software.workshop.rag.api;

public interface QuestionAnsweringService
{
  Answer askQuestion(Question question);
}
