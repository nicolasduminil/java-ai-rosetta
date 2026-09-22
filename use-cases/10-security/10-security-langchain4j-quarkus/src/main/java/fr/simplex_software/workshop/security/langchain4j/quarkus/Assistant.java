package fr.simplex_software.workshop.security.langchain4j.quarkus;

import dev.langchain4j.service.*;
import dev.langchain4j.service.guardrail.*;
import io.quarkiverse.langchain4j.*;

/**
 * The declarative Quarkus AI service. The guardrails and the tool are declared on the service itself, the
 * same first-class mechanism LangChain4j offers on Spring Boot: the Quarkus extension is what wires them in.
 */
@RegisterAiService(tools = AuditLogTool.class)
public interface Assistant
{
  @SystemMessage("""
    You are a helpful assistant for Aurora Labs. Answer the user's question using the context below and the
    available tools. If the answer is not in the context and no tool provides it, say that you don't know.
    Answer concisely.

    CONTEXT:
    {context}""")
  @InputGuardrails(InjectionGuardrail.class)
  @OutputGuardrails(LeakGuardrail.class)
  String answer(@V("context") String context, @UserMessage String question);
}
