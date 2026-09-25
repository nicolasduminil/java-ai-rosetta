package fr.simplex_software.workshop.ragclassic.langchain4j.boot;

import dev.langchain4j.data.message.*;
import dev.langchain4j.data.segment.*;
import dev.langchain4j.model.chat.*;
import dev.langchain4j.model.embedding.*;
import dev.langchain4j.store.embedding.*;
import fr.simplex_software.workshop.rag.api.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import java.util.stream.*;

@Service
public class LangChain4jRagService implements QuestionAnsweringService
{
  private static final Logger LOG = LoggerFactory.getLogger(LangChain4jRagService.class);
  private static final int MAX_RESULTS = 4;
  private static final String SYSTEM_PROMPT = """
    You are a helpful assistant. Answer the user's question using ONLY the context below.
    If the answer is not contained in the context, say that you don't know.
    Answer concisely.

    CONTEXT:
    %s""";

  private final ChatModel chatModel;
  private final EmbeddingModel embeddingModel;
  private final EmbeddingStore<TextSegment> embeddingStore;

  public LangChain4jRagService(ChatModel chatModel, EmbeddingModel embeddingModel,
    EmbeddingStore<TextSegment> embeddingStore)
  {
    this.chatModel = chatModel;
    this.embeddingModel = embeddingModel;
    this.embeddingStore = embeddingStore;
  }

  @Override
  public Answer askQuestion(Question question)
  {
    return new Answer(chatModel.chat(
      SystemMessage.from(SYSTEM_PROMPT.formatted(retrieveContext(question.question()))),
      UserMessage.from(question.question())).aiMessage().text());
  }

  private String retrieveContext(String query)
  {
    var matches = embeddingStore.search(EmbeddingSearchRequest.builder()
      .queryEmbedding(embeddingModel.embed(query).content())
      .maxResults(MAX_RESULTS)
      .build()).matches();
    LOG.info("Retrieved {} chunk(s) for query \"{}\"", matches.size(), query);
    return matches.stream().map(match -> match.embedded().text()).collect(Collectors.joining("\n\n"));
  }
}
