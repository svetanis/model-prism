package com.github.svetanis.models.demo;

import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import com.google.adk.agents.LlmAgent;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import java.util.List;

// mvn exec:java -Dexec.mainClass=com.github.svetanis.models.demo.DemoApp

/**
 * Minimal single-turn demo showing how to wire model-prism into an ADK agent.
 *
 * <p>Demonstrates the basic lifecycle:
 * <ol>
 *   <li>{@link ModelProviderRegistry#registerAll()} discovers all providers on the classpath.</li>
 *   <li>An {@link LlmAgent} is built with a prefixed model name (e.g. {@code "groq/llama-3.1-8b-instant"}).</li>
 *   <li>The agent is run with a simple prompt and the response is printed to stdout.</li>
 * </ol>
 *
 * @see DemoRunner
 */
public final class DemoApp {

  // to be wired via application.properties, e.g.
  // "ollama/llama3", "groq/llama-3.1-8b-instant", "gemini-2.5-flash", "openrouter/auto"
  private static final String MODEL = "groq/llama-3.1-8b-instant";

  public static void main(String[] args) {
    // One call - discovers and registers ALL providers on the classpath
    List<ModelProvider> registered = ModelProviderRegistry.registerAll();
    showProviders(registered);

    LlmAgent agent = demoAgent();
    String prompt = "Reply in one sentence: what is the Java ServiceLoader pattern?";
    // prompt = "What is your name and what can you do?";
    showAgent(agent, prompt);
    DemoRunner.run(agent, prompt);
  }

  private static LlmAgent demoAgent() {
    return LlmAgent.builder()
        .name("demo-agent") //
        .description("Helpful Assistant agent") //
        .model(MODEL)
        .instruction("You are a helpful assistant") //
        .build();
  }
}
