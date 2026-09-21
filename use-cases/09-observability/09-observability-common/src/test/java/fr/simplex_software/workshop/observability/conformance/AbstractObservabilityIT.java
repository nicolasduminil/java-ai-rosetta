package fr.simplex_software.workshop.observability.conformance;

import fr.simplex_software.workshop.observability.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * The shared conformance test drives the observability workload over REST. It proves the RAG workload runs
 * (a corpus fact is retrieved and answered), that the vector store was populated, and that a token-usage
 * metric is exposed at the stack's Prometheus endpoint after a model call. The metric name is the
 * OpenTelemetry GenAI convention {@code gen_ai.client.token.usage} on all three stacks: Spring AI and
 * Quarkus-LangChain4j emit it built-in, and the LangChain4j-on-Boot stack hand-wires a meter of the same
 * name, so this single assertion is uniform. The scrape path differs (Boot {@code /actuator/prometheus},
 * Quarkus {@code /q/metrics}) and is supplied by the subclass.
 */
public abstract class AbstractObservabilityIT
{
  protected abstract URI baseUri();

  protected abstract String metricsPath();

  @Test
  @DisplayName("answers a corpus fact retrieved from the vector store")
  protected void answersFromTheRetrievedCorpus()
  {
    ask()
      .body(new Question("In which city is Aurora Labs headquartered? Answer with the city name only."))
      .when().post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("reykjavik"));
  }

  @Test
  @DisplayName("answers another corpus fact retrieved from the vector store")
  protected void answersAnotherCorpusFact()
  {
    ask()
      .body(new Question("What is the name of Aurora Labs' product? Answer with the product name only."))
      .when().post("/ask")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("lumen"));
  }

  @Test
  @DisplayName("exposes the token-usage metric after a model call")
  protected void exposesTokenUsageMetric()
  {
    ask().body(new Question("Answer with the word yes.")).when().post("/ask").then().statusCode(200);

    given().baseUri(baseUri().toString())
      .when().get(metricsPath())
      .then()
      .statusCode(200)
      .body(containsString("gen_ai_client_token_usage"));
  }

  @Test
  @DisplayName("reports a populated vector store")
  protected void reportsAPopulatedVectorStore()
  {
    given().baseUri(baseUri().toString())
      .when().get("/vectorstore/count")
      .then()
      .statusCode(200)
      .body("count", greaterThan(0));
  }

  @Test
  @DisplayName("rejects a blank question")
  protected void rejectsABlankQuestion()
  {
    ask().body("{\"question\":\"   \"}").when().post("/ask").then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload")
  protected void rejectsAMalformedPayload()
  {
    ask().body("{ this is not json").when().post("/ask").then().statusCode(400);
  }

  private RequestSpecification ask()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
