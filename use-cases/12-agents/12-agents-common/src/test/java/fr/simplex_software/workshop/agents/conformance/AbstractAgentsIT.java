package fr.simplex_software.workshop.agents.conformance;

import fr.simplex_software.workshop.agents.api.*;
import io.restassured.builder.*;
import io.restassured.config.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractAgentsIT
{
  private static final RequestSpecification BASE = new RequestSpecBuilder()
    .setConfig(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
      .setParam("http.socket.timeout", 180000)
      .setParam("http.connection.timeout", 10000)))
    .setContentType(ContentType.JSON)
    .build();

  protected abstract URI baseUri();

  @Test
  @DisplayName("chaining: produces a headline and a summary through sequential steps")
  protected void chainsSequentialSteps()
  {
    request()
      .body(new TextRequest("""
        Aurora Labs, based in Reykjavik, released its data platform Lumen last spring.
        Lumen is now used by hundreds of companies, the launch doubled the company's revenue and led to 
        fifty new hires across engineering and support."""))
      .when().post("/chain")
      .then().statusCode(200)
      .body("headline", not(emptyOrNullString()))
      .body("summary", not(emptyOrNullString()));
  }

  @Test
  @DisplayName("routing: a billing complaint is classified BILLING and answered")
  protected void routesToTheRightSpecialist()
  {
    request()
      .body(new TextRequest("""
        I was charged twice for my monthly subscription this month and I would like a
        refund for the duplicate payment."""))
      .when().post("/route")
      .then().statusCode(200)
      .body("category", equalTo("BILLING"))
      .body("answer", not(emptyOrNullString()));
  }

  @Test
  @DisplayName("parallelization: fans out summary, sentiment and key points into one report")
  protected void fansOutIndependentAnalyses()
  {
    request()
      .body(new TextRequest("""
        The new update is fantastic. The interface is faster, the export finally works, 
        and support replied within minutes. Easily the best release so far."""))
      .when().post("/analyze")
      .then().statusCode(200)
      .body("summary", not(emptyOrNullString()))
      .body("sentiment", anyOf(equalTo("POSITIVE"), equalTo("NEGATIVE"), equalTo("NEUTRAL")))
      .body("keyPoints.size()", greaterThanOrEqualTo(1));
  }

  @Test
  @DisplayName("self-planning: uses a tool to answer a goal it cannot answer on its own")
  protected void plansAndUsesATool()
  {
    request()
      .body(new PlanRequest("Use the catalog tool to look up the price of the WidgetPro, then tell me the price."))
      .when().post("/plan")
      .then().statusCode(200)
      .body("answer", containsString("499"));
  }

  @Test
  @DisplayName("rejects blank text with 400")
  protected void rejectsBlankText()
  {
    request().body("{\"text\":\"   \"}").when().post("/chain").then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a blank goal with 400")
  protected void rejectsBlankGoal()
  {
    request().body("{\"goal\":\"   \"}").when().post("/plan").then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload with 400")
  protected void rejectsMalformedPayload()
  {
    request().body("{ this is not json").when().post("/chain").then().statusCode(400);
  }

  private RequestSpecification request()
  {
    return given().spec(BASE).baseUri(baseUri().toString());
  }
}
