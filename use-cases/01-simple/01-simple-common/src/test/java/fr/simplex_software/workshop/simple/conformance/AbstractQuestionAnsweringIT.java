package fr.simplex_software.workshop.simple.conformance;

import fr.simplex_software.workshop.simple.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractQuestionAnsweringIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("answers a factual question")
  protected void answersAFactualQuestion()
  {
    ask()
      .body(new Question("What is the capital of France? Answer with the city name only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("answer", not(blankOrNullString()))
      .body("answer", containsStringIgnoringCase("paris"));
  }

  @Test
  @DisplayName("rejects a blank question")
  protected void rejectsABlankQuestion()
  {
    ask()
      .body("{\"question\":\"   \"}")
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
