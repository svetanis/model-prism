package com.github.svetanis.models.demo;

import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.runner.InMemoryRunner;
import java.util.List;
import java.util.UUID;

/**
 * Multi-turn session demo showing how ADK maintains conversation context across turns.
 *
 * <p>Demonstrates a travel planning assistant that remembers the user's name, destination,
 * and preferences across three consecutive turns on the same session. This validates
 * that the model-prism {@link com.github.svetanis.models.spi.OpenAiCompatibleLlm} correctly serializes and deserializes
 * the growing message history.
 *
 * @see DemoRunner#runTurn
 */
public final class SessionDemoApp {

  // to be wired via application.properties, e.g.
  // "ollama/llama3", "groq/llama-3.1-8b-instant", "gemini-2.5-flash", "openrouter/auto"
  private static final String MODEL = "groq/llama-3.1-8b-instant";

  private static final String INSTRUCTION =
      """
      You are a helpful travel planning assistant.
      Remember everything the user tells you about their trip.
      """;

  private static final String PROMPT1 =
      """
      Hi! My name is Alice. I'm planning a 2-week trip to Italy
      in October. I'm especially interested in history and old-master art,
      museums, galleries, cathedrals, castles and good Italian food.
      """;

  private static final String PROMPT2 =
      """
      Given what I told you, which cities should I prioritise and
      what's the weather like there during my travel month?
      """;

  private static final String PROMPT3 =
      """
      Can you give me a quick summary of everything we've discussed
      about my trip so far?
      """;

  public static void main(String[] args) {
    // One call - discovers and registers ALL providers on the classpath
    List<ModelProvider> registered = ModelProviderRegistry.registerAll();
    showProviders(registered);

    LlmAgent agent = demoAgent();
    showAgent(agent, PROMPT1);

    var runner = new InMemoryRunner(agent);
    var session = UUID.randomUUID().toString();
    var config = RunConfig.builder().autoCreateSession(true).build();

    // --- Turn 1------------------------------------------------------
    DemoRunner.runTurn(runner, config, session, PROMPT1, 1);

    // --- Turn 2------------------------------------------------------
    DemoRunner.runTurn(runner, config, session, PROMPT2, 2);

    // --- Turn 3------------------------------------------------------
    DemoRunner.runTurn(runner, config, session, PROMPT3, 3);
  }

  private static LlmAgent demoAgent() {
    return LlmAgent.builder()
        .name("travel-assistant") //
        .description("Travel Assistant agent") //
        .model(MODEL) //
        .instruction(INSTRUCTION) //
        .build();
  }
}
