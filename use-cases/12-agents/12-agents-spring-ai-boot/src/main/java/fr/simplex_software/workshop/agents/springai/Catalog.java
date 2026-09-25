package fr.simplex_software.workshop.agents.springai;

import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.*;

@Component
public class Catalog
{
  @Tool(description = "Look up the catalog price in euros of a product by its name")
  public String priceOf(@ToolParam(description = "the product name") String product)
  {
    return product != null && product.toLowerCase().contains("widgetpro") ? "499" : "unknown";
  }
}
