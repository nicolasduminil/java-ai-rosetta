package fr.simplex_software.workshop.ragmodular.langchain4j.boot;

import org.apache.camel.builder.*;
import org.springframework.stereotype.*;

/**
 * Tika extracts the document to plain text,
 * then the stack-native loader splits, embeds and stores it.
 */
@Component
public class RagIngestionRoute extends RouteBuilder
{
  private final CorpusVectorLoader loader;

  public RagIngestionRoute(CorpusVectorLoader loader)
  {
    this.loader = loader;
  }

  @Override
  public void configure()
  {
    from("direct:ingest")
      .routeId("corpus-ingest")
      .to("tika:parse?tikaParseOutputFormat=text")
      .bean(loader, "load(${body}, ${header.source})");
  }
}
