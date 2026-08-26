package fr.simplex_software.workshop.templates.langchain4j.boot;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;

import static org.assertj.core.api.Assertions.*;

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
