package fr.simplex_software.workshop.agents.api;

import java.util.*;

public record AnalysisReport(String summary, Sentiment sentiment, List<String> keyPoints)
{
}
