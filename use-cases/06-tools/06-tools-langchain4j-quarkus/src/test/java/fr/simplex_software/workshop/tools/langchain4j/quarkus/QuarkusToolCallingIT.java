package fr.simplex_software.workshop.tools.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.quarkus.*;
import fr.simplex_software.workshop.tools.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
class QuarkusToolCallingIT extends AbstractToolCallingIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
