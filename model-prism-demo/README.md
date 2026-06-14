# model-prism-demo

Demo module for the ModelProvider SPI. 
Fourteen self-contained runnable classes, each
demonstrating a different aspect of the 
ADK + ModelPrism SPI combination.

---

## Demo Classes
| Class | What it shows | Extra prereqs |
|---|---|---|
| `DemoApp` | SPI auto-discovery via `ModelProviderRegistry.registerAll()` - prints registered providers and runs a single turn | - |
| `SessionDemoApp` | Multi-turn conversation memory - same `InMemoryRunner` + sessionId reused across three turns | - |
| `StreamingDemoApp` | SSE token-by-token streaming - partial events printed inline as they arrive | - |
| `ToolsDemoApp` | Three `FunctionTool`s (`getCurrentTime`, `getWeather`, `calculate`) wired to a live agent | - |
| `AgentToolDemoApp` | `GoogleSearchAgentTool`: Groq outer agent delegates live search to a Gemini sub-agent | GEMINI_API_KEY | 
| `HitlEscalationDemoApp` | Human-in-the-Loop PR Approval demo - pauses execution on sensitive actions (e.g. pom.xml changes) | - |
| `McpDemoApp` | MCP filesystem tools via `@modelcontextprotocol/server-filesystem` (stdio) | Node.js + npx |
| `McpSseDemoApp` | Remote MCP SSE connection via `SseServerParameters` (e.g. GitHub Copilot MCP) | COPILOT_GITHUB_TOKEN |
| `SkillSourceDemoApp` | Dynamic tool loading via `SkillSource` - lists and loads available skills at runtime | - |
| `StructuredOutputDemoApp` | `outputSchema` - agent extracts typed JSON (`title`, `director`, `year`, `genre`, `summary`) from a free-text movie blurb | - |
| `ParallelAgentDemoApp` | `ParallelAgent` - historian, scientist, and economist run concurrently on one topic | - |
| `MultiAgentDemoApp` | `SequentialAgent` + `LoopAgent` : researcher -> (writer <-> critic x2); shows `outputKey` + `Instruction.Provider` for inter-agent state passing | GEMINI_API_KEY |
| `ObservabilityDemoApp` | Emits generalized observability telemetry to BigQuery (via Console in demo) | - |
| `CallbacksDemoApp` | `beforeModel`, `afterModel`, `beforeTool`, `afterTool` lifecycle callbacks; includes a guardrail that short-circuits the tool call | - |
| `WebServerDemoApp` | ADK Dev web server (`AdkWebServer`) - chat via browser at `http://localhost:8080` | - |

---

## Prerequisites

```powershell
# OpenRouter (used by all demos)
$env:OPENROUTER_API_KEY = "your_key_here"

# Gemini (used in AgentToolDemoApp and MultiAgentDemoApp to utilize GoogleSearchTool)
$env:GEMINI_API_KEY = "your_key_here"

# Groq (optional - swap model names in the demo classes to use it)
$env:GROQ_API_KEY = "your_key_here"

# Ollama (optional - no key needed, just have it running)
ollama server
```

## Running
All commands run from the **repo root**. On Powershell, quote the `-D` value.

```powershell
# SPI registration demo (prints registered providers, no interface call)
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.DemoApp"

# SessionDemo - multi-turn memory: three turns share one sessionId
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.SessionDemoApp"

# Streaming demo - partial SSE tokens printed inline as they arrive
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.StreamingDemoApp"

# Tool-calling demo - agent calls getCurrentTime, getWeather, and calculate
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.tools.ToolsDemoApp"

# AgentTool-calling demo - Groq -> GoogleSearchAgentTool -> Gemini (requires GEMINI_API_KEY)
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.agenttool.AgentToolDemoApp"

# MCP local Stdio demo via npx/filesystem (requires Node.js)
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.mcp.McpDemoApp"

# MCP remote SSE demo (requires COPILOT_GITHUB_TOKEN)
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.mcp.McpSseDemoApp"

# SkillSource dynamic loading demo
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.skills.SkillSourceDemoApp"

# Structured output demo - typed JSON extracted from a movie blurb
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.structured.StructuredOutputDemoApp"

# Parallel Agent Demo - historian, scientist, economist run concurrently
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.parallel.ParallelAgentDemoApp"

# Multi-agent pipeline demo (SequentialAgent: researcher -> (writer <-> critic x2)
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.multiagent.MultiAgentDemoApp"

# Human in the Loop (HITL) PR Approval Demo
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.hitl.HitlEscalationDemoApp"

# Observability BigQuery / Console plugin demo
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.observability.ObservabilityDemoApp"

# Callbacks demo - beforeModel, afterModel, beforeTool, afterTool; guardrail example
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.callbacks.CallbacksDemoApp"

# ADK Dev Web server - open http://localhost:8080 in the ADK Dev UI
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.WebServerDemoApp"

# Web server demo on a custom port
mvn -pl model-prism-demo exec:java "-Dexec.mainClass=com.github.svetanis.models.demo.WebServerDemoApp" "-Dexec.args=--server.port=9090"
```

On bash:

```bash
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.DemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.SessionDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.StreamingDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.tools.ToolsDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.agenttool.AgentToolDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.hitl.HitlEscalationDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.mcp.McpDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.mcp.McpSseDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.skills.SkillSourceDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.structured.StructuredOutputDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.parallel.ParallelAgentDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.multiagent.MultiAgentDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.observability.ObservabilityDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.callbacks.CallbacksDemoApp
mvn -pl model-prism-demo exec:java -Dexec.mainClass=com.github.svetanis.models.demo.WebServerDemoApp
```

---

## Why `-parameters` in demo/pom.xml

The `maven-compiler-plugin` is configured with `-parameters`:

```xml
<compilerArgs>
  <arg>-parameters</args>
</compilerArgs>
```

This tells `javac` to embed method parameter names in the `.class` file.
Without it, Java reflection only sees generic names like `arg0`, `arg1`, `arg2`.
ADK's `FunctionalTool` uses reflection to build the JSON schema it sends to the model -
if it gets `arg0` instead of `city`, the model has no idea what the parameter means and
tool calling breaks.

With the flag, reflection returns the real names (`city`, `a`, `b`, `operation`), so
the schema is meaningful to the model. The `@schema` annotations on each parameter in 
`ToolsDemoApp` add human-readable description on top of that.

---

## Dependency Structure
```
model-prism-demo
    |
    |--model-prism-core                    (ModelProvider, ModelRegistry, OpenAiCompatibleLlm)
    |--model-prism-groq                    (GroqModelProvider + META-INF/services)
    |--model-prism-ollama                  (OllamaModelProvider + META-INF/services)
    |--model-prism-openrouter              (OpenRouterModelProvider + META-INF/services)
    |--model-prism-vertex-gemini           (VertexGeminiModelProvider + META-INF/services)
    |--model-prism-vertex-openai           (VertexOpenAiModelProvider + META-INF/services)
        |--model-prism-core (transitive)
```

The provider modules are independent of each other. Remove any one and the remaining providers
still register and work. The demo itself has no dependency on any provider class - it only imports `model-prism-core`

The MCP SDK (`io.modelcontextprotocol.sdk:mcp`) is a transitive dependency via `google-adk` -
no extra entry in `pom.xml` is needed to use `MCPToolset`.
