package fr.simplex_software.workshop.simple.springai;

import fr.simplex_software.workshop.simple.api.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class QuestionAnsweringApplicationTests
{
  @Autowired
  private QuestionAnsweringService questionAnsweringService;

  @Test
  void contextLoadsWithTheSpringAiService()
  {
    assertThat(questionAnsweringService).isInstanceOf(SpringAiQuestionAnsweringService.class);
  }
}
