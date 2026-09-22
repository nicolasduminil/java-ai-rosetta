package fr.simplex_software.workshop.security.springai;

import org.springframework.boot.context.event.*;
import org.springframework.context.event.*;
import org.springframework.core.io.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.charset.*;
import java.util.List;

/**
 * On startup, ingest the three owner-tagged corpus files. The file name is the owner: {@code public.txt} is
 * readable by everyone, {@code alice.txt} and {@code bob.txt} only by their owner.
 */
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
    {
      var resource = new ClassPathResource("corpus/" + owner + ".txt");
      loader.load(resource.getContentAsString(StandardCharsets.UTF_8), owner);
    }
  }
}
