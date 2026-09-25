package fr.simplex_software.workshop.tools.langchain4j.boot;

import dev.langchain4j.agent.tool.*;
import fr.simplex_software.workshop.tools.catalog.*;
import org.springframework.stereotype.*;

@Component
public class CatalogTool
{
  @Tool("Return the warehouse shelf reference for a catalog item, given its item code")
  public String shelfReference(@P("the catalog item code, for example A-100") String code)
  {
    return Catalog.shelfReference(code);
  }
}
