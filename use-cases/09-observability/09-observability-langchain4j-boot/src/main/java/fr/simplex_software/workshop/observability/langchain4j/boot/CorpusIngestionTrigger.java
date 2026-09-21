package fr.simplex_software.workshop.observability.langchain4j.boot;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.event.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.charset.*;

@Component
public class CorpusIngestionTrigger
{
  private final CorpusVectorLoader loader;

  @Value("classpath:/corpus/briefing.txt")
  private Resource corpus;

  public CorpusIngestionTrigger(CorpusVectorLoader loader)
  {
    this.loader = loader;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void ingestCorpus() throws IOException
  {
    loader.load(corpus.getContentAsString(StandardCharsets.UTF_8));
  }
}
