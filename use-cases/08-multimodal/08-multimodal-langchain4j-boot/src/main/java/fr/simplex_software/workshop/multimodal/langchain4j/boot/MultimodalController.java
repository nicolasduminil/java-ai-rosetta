package fr.simplex_software.workshop.multimodal.langchain4j.boot;

import fr.simplex_software.workshop.multimodal.api.*;
import jakarta.validation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;

@RestController
public class MultimodalController
{
  private final MultimodalService service;

  public MultimodalController(MultimodalService service)
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
}
