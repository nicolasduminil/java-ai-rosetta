# 08 — Multimodal (audio & images)

Four capabilities beyond text, across the three stacks, all via **OpenAI**:

| Capability | Endpoint | In | Out |
|---|---|---|---|
| Transcribe (speech to text) | `POST /transcribe` | multipart `audio` file | `{"text": "..."}` |
| Describe an image (vision) | `POST /describe` | multipart `image` file + `question` field | `{"answer": "..."}` |
| Generate an image | `POST /generate-image` | `{"prompt": "..."}` | `image/png` bytes |
| Speak (text to speech) | `POST /speak` | `{"text": "..."}` | `audio/mpeg` bytes |

This is the **first OpenAI use case** in the repository. Every other use case (01–07) runs locally on
Ollama, but Ollama serves none of these: no speech-to-text, no text-to-speech, no image generation. So
this one needs an `OPENAI_API_KEY` and leaves the local-first premise behind. That trade is the point of
the chapter.

## The Rosetta comparison

| Capability | Spring AI 2.0.0 | LangChain4j 1.17.2 | Quarkus-LangChain4j 1.12.2 |
|---|---|---|---|
| Transcribe | `OpenAiAudioTranscriptionModel` | `OpenAiAudioTranscriptionModel` | inject `AudioTranscriptionModel` (`audio-transcription-model.*`) |
| **Speak** | `OpenAiAudioSpeechModel` → `byte[]` | **not shipped** | **not shipped** |
| Vision | `ChatClient … .media(mimeType, resource)` | `UserMessage` + `ImageContent` | inject `ChatModel` + `UserMessage`/`ImageContent` |
| Generate image | `OpenAiImageModel` | `OpenAiImageModel` | inject `ImageModel` (`image-model.*`) |

### Findings

- **Text-to-speech is Spring-AI-only.** LangChain4j ships no speech-synthesis model (verified across
  `langchain4j-core`, `langchain4j-open-ai` and `langchain4j-azure-open-ai`: the only audio *model* is
  `AudioTranscriptionModel`, which goes the other way, audio → text). So `/speak` exists only on the
  Spring AI stack; the other two stacks omit the endpoint.
- **Quarkus reaches these as model beans, not AI services.** Unlike the tool/`@RegisterAiService` flow of
  earlier chapters, transcription, vision and image generation are injected model beans configured under
  `quarkus.langchain4j.openai.*`. (Quarkus *does* also offer declarative vision via `@RegisterAiService`
  with `@ImageUrl`; this use case uses the `ChatModel` bean for parity with the Boot stack.)
- **Spring AI 2.0.0 is rebuilt on the official OpenAI Java SDK.** Model names are plain strings; several
  option enums are the SDK's own types; `Media` moved to `org.springframework.ai.content`; speech is under
  `org.springframework.ai.audio.tts.*`. Defaults changed too (`gpt-4o-mini-tts`, `gpt-image-1-mini`).
- **The image model is `gpt-image-1`, and the OpenAI images endpoint no longer takes `response_format`.**
  This account has only `gpt-image-*` models (no dall-e), and `gpt-image` always returns `b64_json` at
  1024-scale sizes. All three frameworks decode that base64 directly (the code also handles a URL response,
  which older dall-e models returned). Spring AI 2.0.0 does **not** honour a per-prompt image-model
  override, so the model is set on the bean via `spring.ai.openai.image.model`.

## Running

The whole test suite calls OpenAI, so it needs a key. Export it, then run the reactor:

```bash
export OPENAI_API_KEY=sk-...
mvn -q verify -pl use-cases/08-multimodal -amd
```

Without a key the capability tests **skip** (they are gated on `OPENAI_API_KEY`) and only the validation
tests run, so the build stays green in CI. Models are pinned cheap (`whisper-1`, `gpt-4o-mini`, `tts-1`,
`gpt-image-1-mini` at 1024×1024 low quality); a full run costs on the order of $0.10, image generation
being the largest part.

The conformance test (`AbstractMultimodalIT`, shared from `08-multimodal-common`) proves each capability
against committed fixtures: `victor-seven.mp3` (spoken "the shelf reference is Victor seven") must
transcribe to contain "victor"; `label-victor-7.png` (a shelf label) must be read back as "VICTOR-7"; a
generated image must be a valid 256×256 PNG; synthesised speech must be MP3 audio.
