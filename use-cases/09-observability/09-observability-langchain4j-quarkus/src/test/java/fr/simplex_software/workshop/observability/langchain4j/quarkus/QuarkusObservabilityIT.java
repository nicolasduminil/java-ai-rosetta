package fr.simplex_software.workshop.observability.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.quarkus.*;
import fr.simplex_software.workshop.observability.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
@WithTestResource(RagStoreTestResource.class)
class QuarkusObservabilityIT extends AbstractObservabilityIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }

  @Override
  protected String metricsPath()
  {
    return "/q/metrics";
  }
}
