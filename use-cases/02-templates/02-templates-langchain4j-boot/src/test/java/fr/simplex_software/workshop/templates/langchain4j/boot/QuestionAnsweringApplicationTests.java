package fr.simplex_software.workshop.templates.langchain4j.boot;

import fr.simplex_software.workshop.templates.api.*;
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
  void contextLoadsWithTheLangChain4jService()
  {
    assertThat(questionAnsweringService).isInstanceOf(LangChain4jQuestionAnsweringService.class);
  }
}
