package fr.simplex_software.workshop.conformance.langchain4j;

import dev.langchain4j.service.*;
import fr.simplex_software.workshop.eval.api.*;

/**
 * The LangChain4j judge. LangChain4j ships no evaluators, so the LLM-as-judge is expressed the
 * idiomatic way: a declarative AI service whose structured return type is the {@link Evaluation}
 * verdict. This is what Spring AI's evaluators do internally, written out by hand.
 */
interface Judge
{
  @UserMessage("""
    Decide whether the ANSWER is relevant to the QUESTION.
    QUESTION: {{question}}
    ANSWER: {{answer}}
    Set pass to true when the answer addresses the question, false otherwise, with a short reason.
    """)
  Evaluation relevancy(@V("question") String question, @V("answer") String answer);

  @UserMessage("""
    Fact-check the ANSWER against the REFERENCE, treated as ground truth.
    REFERENCE: {{reference}}
    ANSWER: {{answer}}
    Set pass to true when the answer is consistent with the reference, false otherwise, with a short reason.
    """)
  Evaluation factualAccuracy(@V("answer") String answer, @V("reference") String reference);
}
