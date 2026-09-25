package fr.simplex_software.workshop.security.springai;

import org.springframework.boot.context.event.*;
import org.springframework.context.event.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.charset.*;
import java.util.List;

@Component
public class CorpusIngestionTrigger
{
  private static final List<String> OWNERS = List.of("public", "alice", "bob");

  private final CorpusVectorLoader loader;

  public CorpusIngestionTrigger(CorpusVectorLoader loader)
  {
    this.loader = loader;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void ingestCorpus() throws IOException
  {
    for (String owner : OWNERS)
      loader.load(new ClassPathResource("corpus/" + owner + ".txt")
        .getContentAsString(StandardCharsets.UTF_8), owner);
  }
}
