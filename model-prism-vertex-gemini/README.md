# model-prism-vertex-gemini

Vertex AI Gemini provider JAR for Google ADK Java, using the
**native google-genai SDK** transport. Drop this on the classpath
and `VertexGeminiModelProvider` auto-registers via
`META-INF/services` — no application code changes required.

---

## Why This Exists

ADK's built-in `Gemini` class transparently routes to either the Gemini Developer API
or Vertex AI depending on environment variables. That implicit routing is convenient
but invisible in agent code — readers of `model: "gemini-2.5-flash"` cannot tell
whether the call hits Vertex or the consumer Gemini API.

This provider makes the choice **explicit and discoverable**. Model strings of the
form `vertex-gemini/<model-id>` unambiguously route through Vertex AI; they appear
in logs, traces, and configuration with provenance baked in.

---

## Usage

Add the dependency and call `ModelProviderRegistry.registerAll()`
once at startup:

```xml
<dependency>
	<groupId>com.github.svetanis</groupId>
	<artifactId>model-prism-vertex-gemini</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
ModelProviderRegistry.registerAll();
```

Then use any Gemini model via Vertex AI in your agents:

```yaml
# agent.yaml
model: vertex-gemini/gemini-2.5-flash
```

---

## Configuration

| Environment variable | Required | Default | Description |
|---|---|---|---|
| `GOOGLE_CLOUD_PROJECT` | Yes | — | Vertex AI-enabled GCP project id |
| `GOOGLE_CLOUD_LOCATION` | Yes | — | Region, e.g. `us-central1` |
| `GOOGLE_APPLICATION_CREDENTIALS` | No | — | Path to service-account key, or use ambient ADC (`gcloud auth application-default login`) |

> **Note:** This provider forces `GOOGLE_GENAI_USE_VERTEXAI=true` via
> `System.setProperty()` as a defence against accidental routing through
> the Developer API when `GOOGLE_API_KEY` is also set.

---

## Model Names

Prefix any Gemini model name with `vertex-gemini/`.
The prefix is stripped before being passed to the underlying ADK `Gemini` class:

| Model string | Resolves to |
|---|---|
| `vertex-gemini/gemini-2.5-flash` | `gemini-2.5-flash` via Vertex |
| `vertex-gemini/gemini-2.5-pro` | `gemini-2.5-pro` via Vertex |

Available models: https://cloud.google.com/vertex-ai/generative-ai/docs/learn/models

---

## Streaming, Tools, and Structured Output

All delegated to the underlying ADK `Gemini` implementation. This provider adds no
behaviour of its own beyond registration and prefix handling. All ADK features
(function calling, `GoogleSearchTool`, streaming, structured output) work exactly
as they do with the built-in `Gemini` class.

---

## How It Works

This JAR contains a single `META-INF/services` entry:

```
META-INF/services/com.github.svetanis.models.spi.ModelProvider
	|--com.github.svetanis.models.vertex.gemini.VertexGeminiModelProvider
```

When `ModelProviderRegistry.registerAll()` runs, `ServiceLoader` finds
this entry and registers pattern `vertex-gemini/.*` with `LlmRegistry`.
`VertexGeminiModelProvider` implements `ModelProvider`
which handles prefix stripping, then delegates to ADK's native `Gemini`
class for all HTTP transport and response handling.

Note that authentication and short-lived token generation are shared via
the `model-prism-vertex-common` module to prevent duplicating Google Cloud
dependencies.

---

## When to Use This vs. Built-in Gemini

| | Built-in `Gemini` | `vertex-gemini/` provider |
|---|---|---|
| Configuration | Implicit — env vars decide routing | Explicit — model string declares intent |
| Discoverability | `model: "gemini-2.5-flash"` — could be either API | `model: "vertex-gemini/gemini-2.5-flash"` — unambiguous |
| Registration | Automatic (built into ADK) | Via `ModelProviderRegistry.registerAll()` |
| Features | Full ADK feature set | Same — delegates to `Gemini` internally |
