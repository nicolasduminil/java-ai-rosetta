package fr.simplex_software.workshop.ragmodular.conformance;

import fr.simplex_software.workshop.ragmodular.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractModularRagQuestionAnsweringIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("answers a fact found only in the secondary source, surfaced by multi-source retrieval")
  protected void answersFactFromTheSecondarySource()
  {
    ask()
      .body(new Question(
        "What is the internal codename of Lumen's telemetry analytics engine? Answer with the codename only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("borealis"));
  }

  @Test
  @DisplayName("answers a fact from the primary source")
  protected void answersFactFromThePrimarySource()
  {
    ask()
      .body(new Question(
        "In which city is Aurora Labs headquartered? Answer with the city name only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("reykjavik"));
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
