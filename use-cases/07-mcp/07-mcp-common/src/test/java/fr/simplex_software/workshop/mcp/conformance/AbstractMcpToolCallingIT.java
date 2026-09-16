package fr.simplex_software.workshop.mcp.conformance;

import fr.simplex_software.workshop.mcp.api.*;
import fr.simplex_software.workshop.mcp.catalog.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractMcpToolCallingIT
{
  protected abstract URI baseUri();

  @ParameterizedTest(name = "item {0} yields its own shelf reference")
  @ValueSource(strings = {"A-100", "B-200"})
  @DisplayName("calls the remote MCP tool and routes the argument to it")
  protected void callsRemoteToolAndRoutesArgument(String code)
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

  private RequestSpecification ask()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
