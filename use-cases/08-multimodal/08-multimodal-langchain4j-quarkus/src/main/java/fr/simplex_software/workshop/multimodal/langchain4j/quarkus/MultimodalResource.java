package fr.simplex_software.workshop.multimodal.langchain4j.quarkus;

import fr.simplex_software.workshop.multimodal.api.*;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.reactive.*;
import org.jboss.resteasy.reactive.multipart.*;

import java.io.*;
import java.nio.file.Files;

@Path("/")
public class MultimodalResource
{
  private final MultimodalService service;

  public MultimodalResource(MultimodalService service)
  {
    this.service = service;
  }

  @POST
  @Path("/transcribe")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public TranscriptionResult transcribe(@RestForm("audio") FileUpload audio) throws IOException
  {
    return service.transcribe(Files.readAllBytes(audio.uploadedFile()), audio.fileName());
  }

  @POST
  @Path("/describe")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public DescriptionResult describe(@RestForm("image") FileUpload image,
                                    @RestForm("question") String question) throws IOException
  {
    return service.describe(Files.readAllBytes(image.uploadedFile()), question);
  }

  @POST
  @Path("/generate-image")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces("image/png")
  public byte[] generateImage(@Valid GenerateImageRequest request)
  {
    return service.generateImage(request.prompt());
  }
}
