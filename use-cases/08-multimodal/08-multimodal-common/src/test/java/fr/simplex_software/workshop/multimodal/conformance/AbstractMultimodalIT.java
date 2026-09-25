package fr.simplex_software.workshop.multimodal.conformance;

import io.restassured.http.*;
import io.restassured.specification.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;

import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMultimodalIT
{
  private static final String KEY = "OPENAI_API_KEY";

  protected abstract URI baseUri();

  /** Only the Spring AI stack exposes {@code /speak}; other stacks override to keep this {@code false}. */
  protected boolean supportsSpeech()
  {
    return false;
  }

  @Test
  @EnabledIfEnvironmentVariable(named = KEY, matches = ".+")
  @DisplayName("transcribes speech to text")
  protected void transcribesSpeechToText() throws IOException
  {
    multipart()
      .multiPart("audio", "victor-seven.mp3", fixture("/victor-seven.mp3"), "audio/mpeg")
      .when().post("/transcribe")
      .then()
      .statusCode(200)
      .body("text", containsStringIgnoringCase("victor"));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = KEY, matches = ".+")
  @DisplayName("describes a label in an image supplied as context")
  protected void describesLabelInImage() throws IOException
  {
    multipart()
      .multiPart("image", "label-victor-7.png", fixture("/label-victor-7.png"), "image/png")
      .multiPart("question", "What code is on the label? Answer with the code only.")
      .when().post("/describe")
      .then()
      .statusCode(200)
      .body("answer", containsStringIgnoringCase("VICTOR-7"));
  }

  @Test
  @EnabledIfEnvironmentVariable(named = KEY, matches = ".+")
  @DisplayName("generates a valid image of the requested size")
  protected void generatesValidImage() throws IOException
  {
    byte[] png = json().body("{\"prompt\":\"a solid red square\"}")
      .when().post("/generate-image")
      .then()
      .statusCode(200)
      .header("Content-Type", containsString("image/png"))
      .extract().body().asByteArray();

    BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
    assertNotNull(image, "response body is not a readable image");
    assertEquals(1024, image.getWidth());
    assertEquals(1024, image.getHeight());
  }

  @Test
  @EnabledIfEnvironmentVariable(named = KEY, matches = ".+")
  @DisplayName("synthesises speech to audio (Spring AI only)")
  protected void synthesisesSpeechToAudio()
  {
    Assumptions.assumeTrue(supportsSpeech(), "this stack ships no text-to-speech model");

    byte[] audio = json().body("{\"text\":\"The shelf reference is Victor seven.\"}")
      .when().post("/speak")
      .then()
      .statusCode(200)
      .header("Content-Type", containsString("audio/mpeg"))
      .extract().body().asByteArray();

    assertTrue(audio.length > 0, "no audio returned");
    assertTrue(isMp3(audio), "response body is not MP3 audio");
  }

  @Test
  @DisplayName("rejects a blank image prompt")
  protected void rejectsABlankImagePrompt()
  {
    json().body("{\"prompt\":\"   \"}")
      .when().post("/generate-image")
      .then().statusCode(400);
  }

  @Test
  @DisplayName("rejects a blank text to speak")
  protected void rejectsABlankTextToSpeak()
  {
    Assumptions.assumeTrue(supportsSpeech(), "this stack ships no text-to-speech model");

    json().body("{\"text\":\"   \"}")
      .when().post("/speak")
      .then().statusCode(400);
  }

  private RequestSpecification json()
  {
    return given().baseUri(baseUri().toString()).contentType(ContentType.JSON);
  }

  private RequestSpecification multipart()
  {
    return given().baseUri(baseUri().toString());
  }

  private static byte[] fixture(String classpathResource) throws IOException
  {
    try (InputStream in = AbstractMultimodalIT.class.getResourceAsStream(classpathResource))
    {
      if (in == null)
        throw new FileNotFoundException("missing test fixture on classpath: " + classpathResource);
      return in.readAllBytes();
    }
  }

  /** True for an MP3 stream: an ID3 tag ("ID3") or an MPEG audio frame sync (0xFFEx). */
  private static boolean isMp3(byte[] bytes)
  {
    if (bytes.length < 3)
      return false;
    boolean id3 = bytes[0] == 'I' && bytes[1] == 'D' && bytes[2] == '3';
    boolean frameSync = (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xE0) == 0xE0;
    return id3 || frameSync;
  }
}
