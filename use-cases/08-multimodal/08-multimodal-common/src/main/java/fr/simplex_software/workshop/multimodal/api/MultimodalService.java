package fr.simplex_software.workshop.multimodal.api;

public interface MultimodalService
{
  TranscriptionResult transcribe(byte[] audio, String filename);

  DescriptionResult describe(byte[] image, String question);

  byte[] generateImage(String prompt);
}
