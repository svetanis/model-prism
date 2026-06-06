# model-prism-groq

Groq provider JAR for Google ADK Java. 
Drop this on the classpath and
`GroqModelProvider` auto-registers
via `META-INF/services` - 
no application code changes required.

---

## Usage

Add the dependency and call `ModelProviderRegistry.registerAll()` 
once at startup:

```xml
<dependency>
	<groupId>com.github.svetanis</groupId>
	<artifactId>model-prism-groq</artifactId>
	<version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
ModelProviderRegistry.registerAll();
```

Then use any Groq model in your agents:

```yaml
# agent.yaml
model: groq/llama-3.1-8b-instant
```

---

## Configuration

| Environment variable | Required | Description |
|---|---|---|
|`GROQ_API_KEY` | Yes | API key from https://console.groq.com |

---

## Model Names

Prefix any Groq model name with `groq/`. 

Full model list: https://console.groq.com/docs/models

---

## How It Works

This JAR contains a single `META-INF/services` entry:

```
META-INF/services/com.github.svetanis.models.spi.ModelProvider
	|--com.github.svetanis.models.groq.GroqModelProvider
```

When `ModelProviderRegistry.registerAll()` runs, `ServiceLoader` finds 
this entry and registers pattern `groq/.*` with `LlmRegistry`,
`GroqModelProvider` delegates all HTTP and JSON work to 
`OpenAiCompatibleLlm` in the `model-prism-core` module.
