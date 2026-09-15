package fr.simplex_software.workshop.tools.langchain4j.quarkus;

import dev.langchain4j.agent.tool.*;
import fr.simplex_software.workshop.tools.catalog.*;
import jakarta.enterprise.context.*;

/**
 * The catalog tool as a CDI bean. On Quarkus the tool is a normal {@code @ApplicationScoped} bean whose
 * {@code @Tool} method is referenced from {@code @RegisterAiService(tools = CatalogTool.class)} on the
 * {@link Assistant}.
 */
@ApplicationScoped
public class CatalogTool
{
  @Tool("Return the warehouse shelf reference for a catalog item, given its item code")
  public String shelfReference(@P("the catalog item code, for example A-100") String code)
  {
    return Catalog.shelfReference(code);
  }
}
