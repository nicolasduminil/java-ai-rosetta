package fr.simplex_software.workshop.ragclassic.springai;

import org.apache.camel.builder.*;
import org.springframework.stereotype.*;


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
      .bean(loader, "load");
  }
}
