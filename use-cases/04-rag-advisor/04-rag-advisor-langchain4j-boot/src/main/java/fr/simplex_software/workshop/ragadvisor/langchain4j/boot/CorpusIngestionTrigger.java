package fr.simplex_software.workshop.ragadvisor.langchain4j.boot;

import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.event.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * Feeds the bundled corpus PDF into the ingestion pipeline.
 */
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
