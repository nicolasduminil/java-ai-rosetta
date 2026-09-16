package fr.simplex_software.workshop.mcp.server.springai;

import fr.simplex_software.workshop.mcp.catalog.*;
import org.springframework.ai.tool.annotation.*;
import org.springframework.stereotype.*;

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
