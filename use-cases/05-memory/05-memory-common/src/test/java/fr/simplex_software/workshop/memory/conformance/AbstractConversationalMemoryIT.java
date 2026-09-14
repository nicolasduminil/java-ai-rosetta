package fr.simplex_software.workshop.memory.conformance;

import fr.simplex_software.workshop.memory.api.*;
import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;

import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public abstract class AbstractConversationalMemoryIT
{
  protected abstract URI baseUri();

  @Test
  @DisplayName("remembers a fact stated earlier in the same conversation")
  protected void remembersWithinAConversation()
  {
    var conversationId = "conv-recall";
    chat().body(new Question(conversationId, "My name is Aurora. Remember it."))
      .when().post("/chat")
      .then().statusCode(200);

    chat().body(new Question(conversationId, "What is my name? Answer with the name only."))
      .when().post("/chat")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("aurora"));
  }

  @Test
  @DisplayName("keeps separate conversations isolated")
  protected void keepsConversationsIsolated()
  {
    chat().body(new Question("conv-other", "What is my name? Answer with the name only."))
      .when().post("/chat")
      .then()
      .statusCode(200)
      .body("answer", not(containsStringIgnoringCase("aurora")));
  }

  @Test
  @DisplayName("rejects a blank question")
  protected void rejectsABlankQuestion()
  {
    chat().body("{\"conversationId\":\"conv-x\",\"question\":\"   \"}")
      .when().post("/chat")
      .then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a blank conversation id")
  protected void rejectsABlankConversationId()
  {
    chat().body("{\"conversationId\":\"   \",\"question\":\"What is my name?\"}")
      .when().post("/chat")
      .then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a malformed payload")
  protected void rejectsAMalformedPayload()
  {
    chat().body("{ this is not json")
      .when().post("/chat")
      .then().statusCode(400);
  }

  private RequestSpecification chat()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }
}
