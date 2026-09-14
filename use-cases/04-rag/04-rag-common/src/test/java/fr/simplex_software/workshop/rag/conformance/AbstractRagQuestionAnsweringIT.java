package fr.simplex_software.workshop.rag.conformance;

import fr.simplex_software.workshop.rag.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractRagQuestionAnsweringIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("answers a corpus fact retrieved from the vector store")
  protected void answersFromTheRetrievedCorpus()
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
  @DisplayName("answers another corpus fact retrieved from the vector store")
  protected void answersAnotherCorpusFact()
  {
    ask()
      .body(new Question(
        "What is the name of Aurora Labs' product? Answer with the product name only."))
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("lumen"));
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
