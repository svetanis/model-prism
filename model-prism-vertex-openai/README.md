# model-prism-vertex-openai

Vertex AI provider JAR for Google ADK Java, using Google Cloud's
**OpenAI-compatible chat-completions endpoint**. Drop this on the
classpath and `VertexOpenAiModelProvider` auto-registers via
`META-INF/services` — no application code changes required.

---

## Why This Exists (Alongside `model-prism-vertex-gemini`)

Vertex AI exposes **two surfaces** for Gemini and partner models:

1. **Native google-genai SDK** — used by `model-prism-vertex-gemini`
2. **OpenAI-compatible REST endpoint** — used by this module

The OpenAI-compatible surface is the right pick when:

- You already speak the OpenAI wire format and want to point it at Vertex
  with a base-URL change (e.g. `spring-ai-gateway` pass-through)
- You need a partner model (Llama, Mistral, Anthropic-on-Vertex) without
  pulling in a model-family-specific SDK
- You want a single code path that works against Groq, OpenRouter, Ollama,
  *and* Vertex — useful for cross-provider evaluation suites

---

## Usage

Add the dependency and call `ModelProviderRegistry.registerAll()`
once at startup:

```xml
<dependency>
	<groupId>com.github.svetanis</groupId>
	<artifactId>model-prism-vertex-openai</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
ModelProviderRegistry.registerAll();
```

Then use any Vertex AI model via the OpenAI-compatible endpoint:

```yaml
# agent.yaml
model: vertex-openai/gemini-2.5-flash
```

---

## Configuration

| Environment variable | Required | Default | Description |
|---|---|---|---|
| `GOOGLE_CLOUD_PROJECT` | Yes | — | Vertex AI-enabled GCP project id |
| `GOOGLE_CLOUD_LOCATION` | No | `us-central1` | Region for the Vertex endpoint |
| `VERTEX_ACCESS_TOKEN` | No | — | Static bearer token (for CI/local dev) |
| `GOOGLE_APPLICATION_CREDENTIALS` | No | — | Path to service-account key, or use ambient ADC |

### Authentication

Token acquisition is handled by `AccessTokenSupplier.autoDetect()`:

1. If `VERTEX_ACCESS_TOKEN` is set and non-blank → uses that static token
2. Otherwise → uses Google Application Default Credentials (ADC) with
   automatic refresh via `GoogleCredentials`

For local development:
```bash
gcloud auth application-default login
```

---

## Model Names

Prefix any model name with `vertex-openai/`.
The prefix is stripped; the remainder is sent as the `model` field to the
Vertex OpenAI-compatible endpoint:

| Model string | Resolves to |
|---|---|
| `vertex-openai/gemini-2.5-flash` | `gemini-2.5-flash` |
| `vertex-openai/gemini-2.5-pro` | `gemini-2.5-pro` |

Vertex's OpenAI surface also supports partner models hosted on Vertex AI
(availability depends on your project configuration).

---

## Endpoint URL

Constructed from environment variables:

```
https://{LOCATION}-aiplatform.googleapis.com/v1/projects/{PROJECT}/locations/{LOCATION}/endpoints/openapi/chat/completions
```

Example with default location:
```
https://us-central1-aiplatform.googleapis.com/v1/projects/my-project/locations/us-central1/endpoints/openapi/chat/completions
```

---

## How It Works

This JAR contains a single `META-INF/services` entry:

```
META-INF/services/com.github.svetanis.models.spi.ModelProvider
	|--com.github.svetanis.models.vertex.openai.VertexOpenAiModelProvider
```

When `ModelProviderRegistry.registerAll()` runs, `ServiceLoader` finds
this entry and registers pattern `vertex-openai/.*` with `LlmRegistry`.
`VertexOpenAiModelProvider` implements `ModelProvider`
which handles prefix stripping, then creates an `OpenAiCompatibleLlm`
wired with classes provided by the shared `model-prism-vertex-common` module:

- `RefreshingBearerHttpClient` – injects a dynamically-refreshed OAuth2
  bearer token into every request via `AccessTokenSupplier`
- `DefaultOpenAiMessageSerializer` – standard OpenAI JSON serialization

---

## When to Use This vs. `vertex-gemini/`

| | `vertex-gemini/` | `vertex-openai/` |
|---|---|---|
| Transport | Native google-genai SDK | OpenAI-compatible REST |
| Authentication | Ambient ADC via SDK | `AccessTokenSupplier` (env var or ADC) |
| Partner models | Gemini only | Gemini + partner models (Llama, Claude, ...) |
| Wire format | Google proprietary | Standard OpenAI JSON |
| Code path | Separate from other providers | Same as Groq/Ollama/OpenRouter |
| Best for | Gemini-specific features | Cross-provider consistency, partner models |
