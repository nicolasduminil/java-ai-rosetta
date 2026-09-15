package fr.simplex_software.workshop.tools.conformance;

import fr.simplex_software.workshop.tools.api.*;
import fr.simplex_software.workshop.tools.catalog.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * The shared test pins the two properties that define tool-driven generation. It POSTs to {@code /ask}
 * and asserts that the assistant calls the catalog tool and returns its result. Running it for two
 * different item codes also proves the model routes the argument it extracts from the question: a stub
 * that ignored the argument and returned a constant would pass one code but fail the other.
 */
public abstract class AbstractToolCallingIT
{
  protected abstract URI baseUri();

  @ParameterizedTest(name = "item {0} yields its own shelf reference")
  @ValueSource(strings = {"A-100", "B-200"})
  @DisplayName("calls the tool and routes the argument to it")
  protected void callsToolAndRoutesArgument(String code)
  {
    ask().body(new Question("What is the shelf reference for item " + code + "? Answer with the reference only."))
      .when().post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase(Catalog.shelfReference(code)));
  }

  @Test
  @DisplayName("rejects a blank question")
  protected void rejectsABlankQuestion()
  {
    ask().body("{\"question\":\"   \"}")
      .when().post("/ask")
      .then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload")
  protected void rejectsAMalformedPayload()
  {
    ask().body("{ this is not json")
      .when().post("/ask")
      .then().statusCode(400);
  }

  private RequestSpecification ask()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
