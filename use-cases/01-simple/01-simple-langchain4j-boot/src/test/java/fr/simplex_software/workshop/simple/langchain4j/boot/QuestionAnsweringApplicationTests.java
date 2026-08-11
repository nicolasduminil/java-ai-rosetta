package fr.simplex_software.workshop.simple.langchain4j.boot;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Wiring check without Docker. It is a sharper test here than on the Spring AI track: the
 * ChatModel bean only exists because application.properties supplies a base-url, so this failing
 * is the symptom of that property going missing.
 */
@SpringBootTest
class QuestionAnsweringApplicationTests
{
  @Autowired
  private AskController askController;

  @Test
  void contextLoadsWithTheLangChain4jService()
  {
    assertThat(askController).isNotNull();
  }
}
