package fr.simplex_software.workshop.multimodal.langchain4j.quarkus;

import fr.simplex_software.workshop.multimodal.conformance.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
class QuarkusMultimodalIT extends AbstractMultimodalIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
