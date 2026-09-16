package fr.simplex_software.workshop.mcp.catalog;

import java.util.*;

public final class Catalog
{
  public static final String UNKNOWN = "no such item";

  private static final Map<String, String> SHELVES = Map.of(
    "A-100", "QUARTZ-7",
    "B-200", "GRANITE-3");

  private Catalog()
  {
  }

  public static String shelfReference(String code)
  {
    return SHELVES.getOrDefault(code == null ? "" : code.trim().toUpperCase(), UNKNOWN);
  }
}
