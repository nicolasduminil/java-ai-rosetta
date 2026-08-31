package fr.simplex_software.workshop.eval.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.*;
import fr.simplex_software.workshop.conformance.langchain4j.*;
import fr.simplex_software.workshop.conformance.quarkus.*;
import fr.simplex_software.workshop.eval.api.*;
import fr.simplex_software.workshop.eval.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
class LangChain4jAnswerEvaluationIT extends AbstractAnswerEvaluationIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }

  @Override
  protected AnswerEvaluator createEvaluator()
  {
    return LangChain4jAnswerEvaluator.create(
      OllamaTestSupport.baseUrl(), OllamaTestSupport.judgeModel());
  }
}
