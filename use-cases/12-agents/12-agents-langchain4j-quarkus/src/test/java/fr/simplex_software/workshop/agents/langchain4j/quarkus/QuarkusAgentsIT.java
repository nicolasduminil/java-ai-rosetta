package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import fr.simplex_software.workshop.agents.conformance.*;
import fr.simplex_software.workshop.conformance.quarkus.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
class QuarkusAgentsIT extends AbstractAgentsIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
