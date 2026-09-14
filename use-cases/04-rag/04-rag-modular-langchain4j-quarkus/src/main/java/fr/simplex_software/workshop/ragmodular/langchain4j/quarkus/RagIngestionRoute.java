package fr.simplex_software.workshop.ragmodular.langchain4j.quarkus;

import jakarta.enterprise.context.*;
import jakarta.inject.*;
import org.apache.camel.builder.*;

/**
 * Tika extracts the document to plain text,
 * then the stack-native loader splits, embeds and stores it.
 */
@ApplicationScoped
public class RagIngestionRoute extends RouteBuilder
{
  private final CorpusVectorLoader loader;

  @Inject
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
