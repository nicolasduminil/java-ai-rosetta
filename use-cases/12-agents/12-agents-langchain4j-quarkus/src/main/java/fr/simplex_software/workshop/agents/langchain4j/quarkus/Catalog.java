package fr.simplex_software.workshop.agents.langchain4j.quarkus;

import dev.langchain4j.agent.tool.*;
import jakarta.enterprise.context.*;

@ApplicationScoped
public class Catalog
{
  @Tool("Look up the catalog price in euros of a product by its name")
  public String priceOf(String product)
  {
    return product != null && product.toLowerCase().contains("widgetpro") ? "499" : "unknown";
  }
}
