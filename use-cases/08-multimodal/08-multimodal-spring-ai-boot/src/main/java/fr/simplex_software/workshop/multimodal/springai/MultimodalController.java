package fr.simplex_software.workshop.multimodal.springai;

import fr.simplex_software.workshop.multimodal.api.*;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;

/**
 * The four multimodal endpoints. Audio and image uploads come in as multipart; the generated image and
 * synthesised audio go out as raw bytes with their media type. {@code /speak} exists only here, because
 * Spring AI is the only stack with a text-to-speech model.
 */
@RestController
public class MultimodalController
{
  private final SpringAiMultimodalService service;

  public MultimodalController(SpringAiMultimodalService service)
  {
    this.service = service;
  }

  @PostMapping(path = "/transcribe",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public TranscriptionResult transcribe(@RequestParam("audio") MultipartFile audio) throws IOException
  {
    return service.transcribe(audio.getBytes(), audio.getOriginalFilename());
  }

  @PostMapping(path = "/describe",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public DescriptionResult describe(@RequestParam("image") MultipartFile image,
                                    @RequestParam("question") String question) throws IOException
  {
    return service.describe(image.getBytes(), question);
  }

  @PostMapping(path = "/generate-image",
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.IMAGE_PNG_VALUE)
  public byte[] generateImage(@Valid @RequestBody GenerateImageRequest request)
  {
    return service.generateImage(request.prompt());
  }

  @PostMapping(path = "/speak", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "audio/mpeg")
  public byte[] speak(@Valid @RequestBody SpeakRequest request)
  {
    return service.speak(request.text());
  }
}
