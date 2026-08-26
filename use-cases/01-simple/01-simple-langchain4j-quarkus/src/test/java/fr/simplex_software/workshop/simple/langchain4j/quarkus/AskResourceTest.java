package fr.simplex_software.workshop.simple.langchain4j.quarkus;

import io.quarkus.test.junit.*;
import jakarta.inject.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

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
