package fr.simplex_software.workshop.simple.springai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuestionAnsweringApplicationTests
{
  @Autowired
  private AskController askController;

  @Test
  void contextLoadsWithTheSpringAiService()
  {
    assertThat(askController).isNotNull();
  }
}
