# 03 — Evaluation: relevancy and factual accuracy

Use case 02 produced answers. This one **judges** them. It adds automated, LLM-as-judge evaluation of
two qualities every real AI feature eventually needs to measure:

1. **relevancy** — does the answer actually address the question?
2. **factual accuracy** — is the answer consistent with a ground-truth reference?

Unlike 01 and 02, this use case adds **no new application feature**. The app under test is 02's
templated-Q&A service, unchanged. What is new is the *evaluation layer* — which is exactly how the
original chapter framed it: a test that scores the existing service. So 03 is delivered as its own
four-module use case (like 01 and 02): a neutral capability plus one evaluation module per stack, each
**depending on its 02 application and booting it** to score real answers. Evaluation therefore lives
entirely in 03 — 02 stays about templates, context and structured output.

## Where it came from

[`_original/`](_original) is a `@SpringBootTest` from *Spring AI in Action* that wires Spring AI's
`RelevancyEvaluator` and `FactCheckingEvaluator` against the board-game service. As in 01 and 02 the
domain is dropped; what is kept is the mechanism — score an answer for relevance and factual support.

## The asymmetry that makes this use case interesting

Spring AI ships evaluators as first-class classes. **LangChain4j ships none.** That looks like a
blocker for a "same behaviour, three stacks" project, but it is the opposite: an LLM-as-judge is just
a prompt. Spring AI's evaluators wrap a fixed prompt and parse a pass/fail; on LangChain4j you write
that prompt yourself as a declarative AI service. The capability is identical; the route differs.

| Stack | How it evaluates |
|---|---|
| Spring AI | built-in `RelevancyEvaluator` / `FactCheckingEvaluator` (batteries included) |
| LangChain4j / Boot | hand-rolled judge `AiService` returning a structured `Evaluation` verdict |
| LangChain4j / Quarkus | the *same* judge `AiService`, shared verbatim |

## Structure

The evaluation contract is neutral and lives in the `common` module; each stack module depends on its
02 application, boots it, and scores its answers.

```
03-eval/
  ├── 03-eval-common/              # AnswerEvaluator + Evaluation (main), AbstractAnswerEvaluationIT (test-jar)
  ├── 03-eval-spring-ai-boot/      # SpringAiAnswerEvaluator + eval IT — boots 02's Spring AI app
  ├── 03-eval-langchain4j-boot/    # eval IT — boots 02's LangChain4j Boot app (judge from test-support-langchain4j)
  └── 03-eval-langchain4j-quarkus/ # eval IT — boots 02's LangChain4j Quarkus app (app indexed from its jar)
test-support-langchain4j/          # the shared hand-rolled LangChain4j judge (both LangChain4j stacks)
```

Each stack module depends on its `02-templates-<stack>` counterpart and boots it — `@SpringBootTest`
on the two Boot modules, `@QuarkusTest` on the Quarkus one. Because the Spring Boot applications are
repackaged into runnable jars, the boot stack parents classify the runnable jar as `*-exec.jar` so the
plain classes jar stays depend-able; the Quarkus module indexes the 02 app jar (Quarkus scans only the
current module plus explicitly indexed dependencies) via `quarkus.index-dependency`.

`AbstractAnswerEvaluationIT` POSTs a small set of probes to the running app, extracts each answer, and
runs it through the stack's `AnswerEvaluator`. Because the LangChain4j judge is byte-identical on Boot
and Quarkus, it is shared once in `test-support-langchain4j` — kept out of the framework-neutral
`03-eval-common` so LangChain4j never reaches the Spring AI module's classpath (the same isolation rule
that keeps the Spring and Quarkus BOMs apart).

### The two evaluators, side by side

Spring AI — the evaluators are ready-made; fact-checking takes the reference as the context to check
against:

```java
this.relevancyEvaluator = new RelevancyEvaluator(judgeChatClientBuilder);
this.factCheckingEvaluator = FactCheckingEvaluator.builder(judgeChatClientBuilder).build();
// …
var response = factCheckingEvaluator.evaluate(
    new EvaluationRequest(List.of(new Document(reference)), answer));
return new Evaluation(response.isPass(), response.getFeedback());
```

LangChain4j — the prompt *is* the evaluator, and its structured return type is the verdict:

```java
interface Judge {
  @UserMessage("Decide whether the ANSWER is relevant to the QUESTION. …")
  Evaluation relevancy(@V("question") String question, @V("answer") String answer);

  @UserMessage("Fact-check the ANSWER against the REFERENCE, treated as ground truth. …")
  Evaluation factualAccuracy(@V("answer") String answer, @V("reference") String reference);
}
```

## The judge model

Judging is harder than answering, so the judge is a **separate, larger model**
(`qwen2.5:3b` by default, configurable via `ollama.judge.model`) while the application keeps answering
on `qwen2.5:0.5b`. `OllamaTestSupport.judgeModel()` pulls it **lazily**, so only this use case's
evaluation tests pay for the extra model; use cases 01 and 02 stay on the small model alone.

## The headline finding

**A small local judge is not trustworthy for scoring, and the judge model dominates the outcome.** On
the identical probes, with the identical `qwen2.5:3b` judge, the three stacks produced:

| Probe / check | Spring AI | LangChain4j (Boot & Quarkus) |
|---|---|---|
| Aurora Labs → relevancy | fail, *empty feedback* | fail, "Reykjavik is Iceland's capital, not a company HQ" |
| Aurora Labs → factual | **fail** (wrong), *empty feedback* | **pass**, "consistent with the reference" |
| France → relevancy | fail, *empty feedback* | fail, "answer does not state it is the capital" |
| France → factual | pass, *empty feedback* | pass, "consistent with the reference" |

Every answer is in fact correct, so most of these verdicts are the *judge* being wrong. Two things
stand out. Spring AI's evaluators return **empty feedback** and even miss a blatant fact-check, because
their fixed prompts are not satisfied by a small local model. The hand-rolled LangChain4j judge returns
**reasoned verdicts** and gets both fact-checks right. And the two LangChain4j stacks return
near-identical reasons, which is the equivalence the shared test exists to prove.

Because verdict correctness depends on the judge rather than on the stack under test, the conformance
test asserts the **capability** — each stack runs relevancy and fact-checking and returns a structured
verdict — and **logs** the verdicts rather than gating on them. Asserting correctness would test the
judge model, not the code, and would make the suite non-deterministic. Raising the judge to a capable
hosted model is what a production evaluation would do; that is the lesson, recorded here rather than
hard-coded into an assertion.

## Where they genuinely diverge

| | Spring AI | LangChain4j (both) |
|---|---|---|
| evaluator | supplied by the framework | written as an `AiService` |
| relevancy call | `RelevancyEvaluator.evaluate(EvaluationRequest)` | a prompt method returning `Evaluation` |
| fact-check context | `EvaluationRequest(List<Document>, answer)` | `{{reference}}` bound in the prompt |
| verdict feedback | `EvaluationResponse.getFeedback()` (empty here) | a populated `reason` on every call |
| judge model wiring | a judge `ChatClient.Builder` with overridden options | a hand-built `OllamaChatModel` |

## Status

| Track | State |
|---|---|
| `03-eval-common` | done — `AnswerEvaluator` contract + shared evaluation suite (test-jar) |
| `03-eval-spring-ai-boot` | done — Spring AI evaluators, boots 02's app, passes the capability suite |
| `03-eval-langchain4j-boot` | done — shared judge, boots 02's app, passes the capability suite |
| `03-eval-langchain4j-quarkus` | done — shared judge, boots 02's app (indexed jar), passes the capability suite |

`mvn verify` runs each stack's evaluation against one shared Ollama container holding both the
answering model and the judge model.
