package fr.simplex_software.workshop.tools.springai;

import fr.simplex_software.workshop.tools.catalog.*;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.*;

/**
 * The catalog tool, the Spring AI way: a bean whose {@code @Tool}-annotated method the model may call.
 */
@Component
public class CatalogTool
{
  @Tool(description = "Return the warehouse shelf reference for a catalog item, given its item code")
  public String shelfReference(
    @ToolParam(description = "the catalog item code, for example A-100") String code)
  {
    return Catalog.shelfReference(code);
  }
}
