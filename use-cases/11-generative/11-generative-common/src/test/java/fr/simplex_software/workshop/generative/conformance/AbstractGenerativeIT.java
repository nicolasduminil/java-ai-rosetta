package fr.simplex_software.workshop.generative.conformance;

import fr.simplex_software.workshop.generative.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractGenerativeIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("summarizes text into a headline and key points")
  protected void summarizesIntoAStructure()
  {
    request()
      .body(new TextRequest("Aurora Labs, based in Reykjavik, released its flagship product Lumen last "
        + "spring. Lumen is a data platform now used by hundreds of companies. The launch doubled the "
        + "company's revenue and led to fifty new hires across engineering and support."))
      .when().post("/summarize")
      .then().statusCode(200)
      .body("headline", not(emptyOrNullString()))
      .body("keyPoints.size()", greaterThanOrEqualTo(1));
  }

  @Test
  @DisplayName("translates text and detects the source language")
  protected void translatesText()
  {
    request()
      .body(new TranslateRequest("Good morning, how are you today?", "French"))
      .when().post("/translate")
      .then().statusCode(200)
      .body("detectedSourceLanguage", not(emptyOrNullString()))
      .body("translatedText", containsStringIgnoringCase("bonjour"));
  }

  @Test
  @DisplayName("classifies clearly positive text as POSITIVE")
  protected void classifiesPositiveSentiment()
  {
    request()
      .body(new TextRequest("I absolutely love this product. It is wonderful and exceeded every "
        + "expectation I had."))
      .when().post("/sentiment")
      .then().statusCode(200)
      .body("sentiment", equalTo("POSITIVE"));
  }

  @Test
  @DisplayName("classifies clearly negative text as NEGATIVE")
  protected void classifiesNegativeSentiment()
  {
    request()
      .body(new TextRequest("This is the worst experience I have ever had. Terrible service and the "
        + "product arrived broken."))
      .when().post("/sentiment")
      .then().statusCode(200)
      .body("sentiment", equalTo("NEGATIVE"));
  }

  @Test
  @DisplayName("extracts people, organizations and emails into lists")
  protected void extractsStructuredData()
  {
    request()
      .body(new TextRequest("John Doe from Acme Corporation can be reached at john.doe@acme.com, and "
        + "Jane Smith works at Globex."))
      .when().post("/extract")
      .then().statusCode(200)
      .body("emails", hasItem(equalToIgnoringCase("john.doe@acme.com")))
      .body("people.size()", greaterThanOrEqualTo(1))
      .body("organizations.size()", greaterThanOrEqualTo(1));
  }

  @Test
  @DisplayName("rejects blank text with 400")
  protected void rejectsBlankText()
  {
    request().body("{\"text\":\"   \"}").when().post("/summarize").then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a translation with a blank target language with 400")
  protected void rejectsBlankTargetLanguage()
  {
    request().body("{\"text\":\"Hello\",\"targetLanguage\":\"   \"}")
      .when().post("/translate").then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload with 400")
  protected void rejectsMalformedPayload()
  {
    request().body("{ this is not json").when().post("/summarize").then().statusCode(400);
  }

  private RequestSpecification request()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
