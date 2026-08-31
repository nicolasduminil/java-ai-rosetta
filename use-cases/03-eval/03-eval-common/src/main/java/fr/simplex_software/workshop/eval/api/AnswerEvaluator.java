package fr.simplex_software.workshop.eval.api;

public interface AnswerEvaluator
{
  Evaluation relevancy(String question, String answer);

  Evaluation factualAccuracy(String question, String answer, String reference);
}
