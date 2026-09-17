package fr.simplex_software.workshop.multimodal.springai;

import fr.simplex_software.workshop.multimodal.conformance.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.web.server.*;

import java.net.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringAiMultimodalIT extends AbstractMultimodalIT
{
  @LocalServerPort
  private int port;

  @Override
  protected boolean supportsSpeech()
  {
    return true;
  }

  @Override
  protected URI baseUri()
  {
    return URI.create("http://localhost:" + port);
  }
}
