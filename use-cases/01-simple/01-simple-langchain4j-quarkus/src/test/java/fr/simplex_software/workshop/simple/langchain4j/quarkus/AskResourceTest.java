package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import io.quarkus.test.junit.*;
import jakarta.inject.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Wiring check without Docker, the counterpart of the two Boot tracks'
 * {@code QuestionAnsweringApplicationTests}. Each stack is verified the same way, so the test
 * counts in the comparison mean the same thing.
 *
 * <p>No Ollama is needed: the extension builds its ChatModel from configuration alone and does not
 * contact the server until a question is asked.
 */
@QuarkusTest
class AskResourceTest
{
  @Inject
  AskResource askResource;

  @Test
  void contextLoadsWithTheLangChain4jService()
  {
    assertThat(askResource).isNotNull();
  }
}
