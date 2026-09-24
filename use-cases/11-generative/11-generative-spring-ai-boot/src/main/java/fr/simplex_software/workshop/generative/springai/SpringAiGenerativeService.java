package fr.simplex_software.workshop.generative.springai;

import fr.simplex_software.workshop.generative.api.*;
import org.springframework.ai.chat.client.*;
import org.springframework.stereotype.*;

@Service
public class SpringAiGenerativeService implements GenerativeService
{
  private final ChatClient chatClient;

  public SpringAiGenerativeService(ChatClient.Builder chatClientBuilder)
  {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Summary summarize(TextRequest request)
  {
    return chatClient.prompt()
      .system("You summarise text. Reply with a short headline and three to five concise key points.")
      .user(user -> user.text("Summarise the following text:\n\n{text}").param("text", request.text()))
      .call()
      .entity(Summary.class);
  }

  @Override
  public Translation translate(TranslateRequest request)
  {
    return chatClient.prompt()
      .user(user -> user
        .text("Detect the source language of the text below and translate it into {lang}.\n\nText:\n{text}")
        .param("lang", request.targetLanguage())
        .param("text", request.text()))
      .call()
      .entity(Translation.class);
  }

  @Override
  public SentimentResponse analyzeSentiment(TextRequest request)
  {
    return chatClient.prompt()
      .system("You are a sentiment classifier.")
      .user(user -> user.text("Classify the overall sentiment of this text:\n\n{text}").param("text", request.text()))
      .call()
      .entity(SentimentResponse.class);
  }

  @Override
  public Extraction extract(TextRequest request)
  {
    return chatClient.prompt()
      .user(user -> user
        .text("Extract the people, organizations and email addresses mentioned in the text below.\n\nText:\n{text}")
        .param("text", request.text()))
      .call()
      .entity(Extraction.class);
  }
}
