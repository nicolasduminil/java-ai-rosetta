package fr.simplex_software.workshop.multimodal.langchain4j.boot;

import fr.simplex_software.workshop.multimodal.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LangChain4jMultimodalIT extends AbstractMultimodalIT
{
  @LocalServerPort
  private int port;

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
