package fr.simplex_software.workshop.eval.conformance;

import fr.simplex_software.workshop.eval.api.*;
import fr.simplex_software.workshop.templates.api.*;
import io.restassured.http.*;
import org.junit.jupiter.api.*;
import org.slf4j.*;

import java.net.*;
import java.util.*;
import java.util.stream.*;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.*;

/**
 * The model evaluates its own answers by sending POST requests having probes
 * in their body over HTTP, and the returned answer is
 * scored for relevancy and factual accuracy through the stack's
 * {@link AnswerEvaluator}.
 *
 * <p>The verdict's correctness is deliberately not asserted because the current LLM
 * isn't reliable enough for relevance and factual accuracy.
 */
public abstract class AbstractAnswerEvaluationIT
{
  private static final Logger LOG = LoggerFactory.getLogger(AbstractAnswerEvaluationIT.class);

  protected abstract URI baseUri();

  protected abstract AnswerEvaluator createEvaluator();

  private AnswerEvaluator evaluator;

  @BeforeEach
  void buildEvaluator()
  {
    evaluator = createEvaluator();
  }

  private static final List<Probe> PROBES = List.of(
    new Probe(
      new Question("Aurora Labs", "In which city is it headquartered? Answer with the city name only."),
      "Aurora Labs is headquartered in Reykjavik."),
    new Probe(
      new Question("France", "What is the capital? Answer with the city name only."),
      "The capital of France is Paris."));

  @TestFactory
  Stream<DynamicTest> evaluateRelevancyAndFactualAccuracy()
  {
    return PROBES.stream().map(probe -> dynamicTest(probe.request().topic(), () ->
    {
      var answer = ask(probe.request());
      assertThat(answer).as("answer from the application").isNotBlank();

      var question = probe.request().question();
      var relevancy = evaluator.relevancy(question, answer);
      var factualAccuracy = evaluator.factualAccuracy(question, answer, probe.reference());

      assertThat(relevancy).as("relevancy verdict").isNotNull();
      assertThat(factualAccuracy).as("factual-accuracy verdict").isNotNull();

      LOG.info("[{}] answer=\"{}\" -> relevancy(pass={}, {}) | factualAccuracy(pass={}, {})",
        probe.request().topic(), answer,
        relevancy.pass(), relevancy.reason(),
        factualAccuracy.pass(), factualAccuracy.reason());
    }));
  }

  private String ask(Question request)
  {
    return given()
      .baseUri(baseUri().toString())
      .contentType(ContentType.JSON)
      .body(request)
      .when()
      .post("/ask")
      .then()
      .statusCode(200)
      .extract()
      .path("answer");
  }

  private record Probe(Question request, String reference)
  {
  }
}
