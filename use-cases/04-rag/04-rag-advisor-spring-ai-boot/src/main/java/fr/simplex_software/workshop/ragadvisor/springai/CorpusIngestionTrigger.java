package fr.simplex_software.workshop.ragadvisor.springai;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.event.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;

@Component
public class CorpusIngestionTrigger
{
  private final ProducerTemplate producerTemplate;

  @Value("classpath:/corpus/aurora-labs-briefing.pdf")
  private Resource corpus;

  public CorpusIngestionTrigger(ProducerTemplate producerTemplate)
  {
    this.producerTemplate = producerTemplate;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void ingestCorpus() throws IOException
  {
    try (var in = corpus.getInputStream())
    {
      producerTemplate.sendBody("direct:ingest", in.readAllBytes());
    }
  }
}
