package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

/**
 * The third stack, held to the same contract. The assertions live entirely in
 * {@link AbstractQuestionAnsweringIT}; all this class does is start the application and say where
 * it is listening.
 */
@QuarkusTest
@WithTestResource(OllamaTestResource.class)
class LangChain4jQuestionAnsweringIT extends AbstractQuestionAnsweringIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
