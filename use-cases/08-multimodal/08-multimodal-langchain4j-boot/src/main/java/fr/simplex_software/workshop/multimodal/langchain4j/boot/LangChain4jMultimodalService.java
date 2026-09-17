package fr.simplex_software.workshop.multimodal.langchain4j.boot;

import dev.langchain4j.data.audio.*;
import dev.langchain4j.data.image.*;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.audio.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.image.*;
import fr.simplex_software.workshop.multimodal.api.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;

/**
 * The three modalities LangChain4j supports, each on a hand-wired OpenAI model. LangChain4j's image model
 * exposes no {@code response_format} option, so dall-e-2 returns an URL; the generated image is downloaded
 * to PNG bytes (base64 is honoured too, for models that return it).
 */
@Service
public class LangChain4jMultimodalService implements MultimodalService
{
  private final ChatModel chatModel;
  private final AudioTranscriptionModel transcriptionModel;
  private final ImageModel imageModel;
  private final HttpClient httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(30)).build();

  public LangChain4jMultimodalService(ChatModel chatModel,
                                      AudioTranscriptionModel transcriptionModel,
                                      ImageModel imageModel)
  {
    this.chatModel = chatModel;
    this.transcriptionModel = transcriptionModel;
    this.imageModel = imageModel;
  }

  @Override
  public TranscriptionResult transcribe(byte[] audio, String filename)
  {
    Audio input = Audio.builder().binaryData(audio).mimeType("audio/mpeg").build();
    AudioTranscriptionResponse response = transcriptionModel.transcribe(
      AudioTranscriptionRequest.builder().audio(input).build());
    return new TranscriptionResult(response.text());
  }

  @Override
  public DescriptionResult describe(byte[] image, String question)
  {
    String base64 = Base64.getEncoder().encodeToString(image);
    UserMessage message = UserMessage.from(TextContent.from(question), ImageContent.from(base64, "image/png"));
    return new DescriptionResult(chatModel.chat(message).aiMessage().text());
  }

  @Override
  public byte[] generateImage(String prompt)
  {
    Image image = imageModel.generate(prompt).content();
    if (image.base64Data() != null)
      return Base64.getDecoder().decode(image.base64Data());
    if (image.url() != null)
      return download(image.url());
    throw new IllegalStateException("Image model returned neither base64 data nor a URL");
  }

  private byte[] download(URI uri)
  {
    try
    {
      return httpClient.send(HttpRequest.newBuilder(uri).GET().build(),
        HttpResponse.BodyHandlers.ofByteArray()).body();
    }
    catch (IOException e)
    {
      throw new IllegalStateException("Could not download the generated image from " + uri, e);
    }
    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while downloading the generated image from " + uri, e);
    }
  }
}
