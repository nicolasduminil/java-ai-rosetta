package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import fr.simplex_software.workshop.simple.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

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
