package fr.simplex_software.workshop.tools.catalog;

import java.util.*;

/**
 * The single source of truth for the catalog tool, shared by every stack's tool wrapper and by the
 * conformance test. The shelf references are arbitrary and unguessable, so a question about a catalog
 * item can only be answered correctly by actually calling the tool with the right item code: a correct
 * answer is therefore proof that the tool was invoked, not that the model guessed.
 */
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
