package fr.simplex_software.workshop.multimodal.springai;

import com.openai.models.audio.*;
import fr.simplex_software.workshop.multimodal.api.*;
import org.springframework.ai.audio.transcription.*;
import org.springframework.ai.audio.tts.*;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.image.*;
import org.springframework.ai.openai.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;

import java.util.*;

@Service
public class SpringAiMultimodalService implements MultimodalService
{
  private final OpenAiAudioTranscriptionModel transcriptionModel;
  private final OpenAiAudioSpeechModel speechModel;
  private final OpenAiImageModel imageModel;
  private final ChatClient chatClient;

  public SpringAiMultimodalService(OpenAiAudioTranscriptionModel transcriptionModel,
                                   OpenAiAudioSpeechModel speechModel,
                                   OpenAiImageModel imageModel,
                                   ChatClient.Builder chatClientBuilder)
  {
    this.transcriptionModel = transcriptionModel;
    this.speechModel = speechModel;
    this.imageModel = imageModel;
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public TranscriptionResult transcribe(byte[] audio, String filename)
  {
    Resource resource = new ByteArrayResource(audio)
    {
      @Override
      public String getFilename()
      {
        return filename;
      }
    };
    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(resource,
      OpenAiAudioTranscriptionOptions.builder()
        .model("whisper-1")
        .responseFormat(AudioResponseFormat.TEXT)
        .build());
    return new TranscriptionResult(transcriptionModel.call(prompt).getResult().getOutput());
  }

  @Override
  public DescriptionResult describe(byte[] image, String question)
  {
    String answer = chatClient.prompt()
      .user(u -> u.text(question).media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(image)))
      .call()
      .content();
    return new DescriptionResult(answer);
  }

  @Override
  public byte[] generateImage(String prompt)
  {
    Image image = imageModel.call(new ImagePrompt(prompt)).getResult().getOutput();
    String b64 = image.getB64Json();
    if (b64 == null)
      throw new IllegalStateException("Image model returned no base64 data");
    return Base64.getDecoder().decode(b64);
  }

  public byte[] speak(String text)
  {
    TextToSpeechResponse response = speechModel.call(new TextToSpeechPrompt(text,
      OpenAiAudioSpeechOptions.builder()
        .model("tts-1")
        .voice("alloy")
        .responseFormat("mp3")
        .speed(1.0d)
        .build()));
    return response.getResult().getOutput();
  }
}
