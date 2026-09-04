package fr.simplex_software.workshop.ragmodular.langchain4j.quarkus;

import fr.simplex_software.workshop.conformance.quarkus.*;
import fr.simplex_software.workshop.ragmodular.conformance.*;
import io.quarkus.test.common.*;
import io.quarkus.test.common.http.*;
import io.quarkus.test.junit.*;

import java.net.*;

@QuarkusTest
@WithTestResource(OllamaTestResource.class)
@WithTestResource(RagStoreTestResource.class)
class LangChain4jRagQuestionAnsweringIT extends AbstractModularRagQuestionAnsweringIT
{
  @TestHTTPResource
  URI applicationUri;

  @Override
  protected URI baseUri()
  {
    return applicationUri;
  }
}
