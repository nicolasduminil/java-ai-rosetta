package fr.simplex_software.workshop.templates.conformance;

import fr.simplex_software.workshop.templates.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * The contract every stack is held to. It exercises the three things use case 02 adds over 01:
 * a templated system prompt, an optional reference document folded into it, and structured output.
 *
 * <p>Assertions are semantic, never exact-string: LLM output is non-deterministic even at
 * temperature zero, so the positive cases pin a token any usable model produces rather than a
 * sentence, and pin the {@code topic} field only to prove the model populated the structure.
 */
public abstract class AbstractQuestionAnsweringIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("answers from the injected reference document")
  protected void answersFromTheReference()
  {
    ask()
      .body(new Question("Aurora Labs",
        "In which city is it headquartered? Answer with the city name only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("topic", containsStringIgnoringCase("aurora"))
      .body("answer", containsStringIgnoringCase("reykjavik"));
  }

  @Test
  @DisplayName("answers a neutral question with no reference document")
  protected void answersANeutralQuestion()
  {
    ask()
      .body(new Question("France",
        "What is the capital? Answer with the city name only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("topic", containsStringIgnoringCase("france"))
      .body("answer", containsStringIgnoringCase("paris"));
  }

  @Test
  @DisplayName("rejects a blank question")
  protected void rejectsABlankQuestion()
  {
    ask()
      .body("{\"topic\":\"France\",\"question\":\"   \"}")
      .when()
      .post("/ask")
      .then()
      .statusCode(400);
  }

  @Test
  @DisplayName("rejects a blank topic")
  protected void rejectsABlankTopic()
  {
    ask()
      .body("{\"topic\":\"   \",\"question\":\"What is the capital of France?\"}")
      .when()
      .post("/ask")
      .then()
      .statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload")
  protected void rejectsAMalformedPayload()
  {
    ask()
      .body("{ this is not json")
      .when()
      .post("/ask")
      .then()
      .statusCode(400);
  }

  private RequestSpecification ask()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
