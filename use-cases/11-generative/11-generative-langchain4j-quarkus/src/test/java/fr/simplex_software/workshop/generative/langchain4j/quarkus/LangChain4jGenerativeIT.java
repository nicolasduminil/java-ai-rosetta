package fr.simplex_software.workshop.generative.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.quarkus.*;
import fr.simplex_software.workshop.generative.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
class LangChain4jGenerativeIT extends AbstractGenerativeIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
