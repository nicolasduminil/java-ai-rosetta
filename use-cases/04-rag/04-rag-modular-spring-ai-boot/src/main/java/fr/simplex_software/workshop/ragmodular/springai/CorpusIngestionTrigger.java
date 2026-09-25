package fr.simplex_software.workshop.ragmodular.springai;

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

  @Value("classpath:/corpus/aurora-briefing.pdf")
  private Resource briefing;

  @Value("classpath:/corpus/aurora-press.pdf")
  private Resource press;

  public CorpusIngestionTrigger(ProducerTemplate producerTemplate)
  {
    this.producerTemplate = producerTemplate;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void ingestCorpus() throws IOException
  {
    ingest(briefing, "briefing");
    ingest(press, "press");
  }

  private void ingest(Resource resource, String source) throws IOException
  {
    try (var in = resource.getInputStream())
    {
      producerTemplate.sendBodyAndHeader("direct:ingest", in.readAllBytes(), "source", source);
    }
  }
}
