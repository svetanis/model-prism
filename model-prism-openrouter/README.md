# model-prism-openrouter

OpenRouter provider JAR for Google ADK Java.
Aggregates hundreds of models from many
providers behind a single API - many with 
a free tier. Drop this on the classpath and
`OpenRouterModelProvider` auto-registers
via `META-INF/services`.

---

## Usage

Add the dependency and call `ModelProviderRegistry.registerAll()` 
once at startup:

```xml
<dependency>
	<groupId>com.github.svetanis</groupId>
	<artifactId>model-prism-openrouter</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
ModelProviderRegistry.registerAll();
```

Then use any OpenRouter model in your agents:

```yaml
# agent.yaml
model: openrouter/auto
```

---

## Configuration

| Environment variable | Required | Description |
|---|---|---|
|`OPENROUTER_API_KEY` | Yes | API key from https://openrouter.ai |

---

## Model Names

Prefix any OpenRouter model name with `openrouter/`. 
Models ending in `:free` have no per-token cost (rate-limited):

| Model | Notes |
|---|---|
|`openrouter/meta-llama/llama-3-8b-instruct:free` | Free tier, good general purpose |
|`openrouter/mistralai/mistral-7b-instruct:free` | Free tier, fast |
|`openrouter/google/gemma-3-27b-it:free` | Free tier, strong reasoning |
|`openrouter/meta-llama/llama-3.70b-instruct` | Paid, best Llama 3 quality |
|`openrouter/anthropic/claude-3-haiku` | Paid, fast Claude |

Full model list with pricing: https://openrouter.ai/models

---

## How It Works

This JAR contains a single `META-INF/services` entry:

```
META-INF/services/com.github.svetanis.models.spi.ModelProvider
	|--com.github.svetanis.models.openrouter.OpenRouterModelProvider
```

When `ModelProviderRegistry.registerAll()` runs, `ServiceLoader` finds 
this entry and registers pattern `openrouter/.*` with `LlmRegistry`,
`OpenRouterModelProvider` delegates all HTTP and JSON work to 
`OpenAiCompatibleLlm` in the `model-prism-core` module.
