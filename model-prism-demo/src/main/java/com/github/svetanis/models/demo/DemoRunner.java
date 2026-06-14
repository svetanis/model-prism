package com.github.svetanis.models.demo;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.github.svetanis.models.spi.ModelProvider;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

/**
 * Shared utility class for running ADK agents in demo applications.
 *
 * <p>
 * Provides helper methods for:
 * <ul>
 * <li>Executing single-turn and multi-turn agent runs via
 * {@link InMemoryRunner}</li>
 * <li>Streaming execution with SSE mode</li>
 * <li>Pretty-printing agent events to stdout</li>
 * <li>Displaying registered providers and agent metadata</li>
 * </ul>
 */
public class DemoRunner {

	// to be wired via application.properties, e.g.
	// "ollama/llama3", "groq/llama-3.3-70b-versatile", "gemini-2.5-flash",
	// "openrouter/auto"
	public static final String MODEL = "groq/llama-3.3-70b-versatile";

	private static final String PAT = " %-20s pattern: %s%n";

	private DemoRunner() {
	}

	/**
	 * Runs a single-turn agent interaction with default event printing.
	 *
	 * @param agent  the agent to run
	 * @param prompt the user prompt
	 */
	public static void run(BaseAgent agent, String prompt) {
		run(agent, prompt, DemoRunner::printEvent);
	}

	/**
	 * Runs a single conversation turn on an existing session, printing turn
	 * metadata.
	 *
	 * @param runner    the pre-initialised runner
	 * @param config    run configuration
	 * @param sessionId the session to continue
	 * @param prompt    the user prompt for this turn
	 * @param turn      the 1-based turn number (for display)
	 */
	public static void runTurn(InMemoryRunner runner, RunConfig config, String sessionId, String prompt, int turn) {
		System.out.println("%nTurn %d - User: %s%n".formatted(turn, prompt));
		System.out.println("-".repeat(70));
		var content = Content.fromParts(Part.fromText(prompt));
		runner.runAsync("demo-user", sessionId, content, config).blockingForEach(DemoRunner::printEvent);
	}

	/**
	 * Runs a single-turn agent interaction with a custom event handler.
	 *
	 * @param agent   the agent to run
	 * @param prompt  the user prompt
	 * @param handler callback invoked for each emitted {@link Event}
	 */
	public static void run(BaseAgent agent, String prompt, Consumer<Event> handler) {
		var runner = new InMemoryRunner(agent);
		var session = UUID.randomUUID().toString();
		var content = Content.fromParts(Part.fromText(prompt));
		var config = RunConfig.builder().autoCreateSession(true).build();
		runner.runAsync("demo-user", session, content, config).blockingForEach(handler::accept);
	}

	/**
	 * Prints an event to stdout. Partial tokens are printed inline; final
	 * (turn-complete) events are printed with a header and separator.
	 *
	 * @param event the event to print
	 */
	public static void printEvent(Event event) {
		String author = event.author();
		String content = event.stringifyContent();
		boolean isPartial = event.partial().orElse(false);
		boolean isTurnComplete = event.turnComplete().orElse(false);
		if (isBlank(content)) {
			return;
		}
		if (isPartial) {
			System.out.println(content);
		} else if (isTurnComplete) {
			System.out.println();
			System.out.println("-".repeat(70));
			System.out.printf("[%s - final response]%n", author);
			System.out.println(content);
		} else {
			System.out.printf("[%s] %s%n", author, content);
		}
	}

	/**
	 * Runs an agent interaction in SSE streaming mode.
	 *
	 * @param agent   the agent to run
	 * @param prompt  the user prompt
	 * @param handler callback invoked for each emitted {@link Event}
	 */
	public static void runStreaming(BaseAgent agent, String prompt, Consumer<Event> handler) {
		var runner = new InMemoryRunner(agent);
		var session = UUID.randomUUID().toString();
		var content = Content.fromParts(Part.fromText(prompt));
		var config = RunConfig.builder().autoCreateSession(true).streamingMode(RunConfig.StreamingMode.SSE).build();
		runner.runAsync("demo-user", session, content, config).blockingForEach(handler::accept);
	}

	/**
	 * Prints a streaming event, accumulating partial tokens inline and printing a
	 * summary when the turn completes.
	 *
	 * @param event the streaming event
	 * @param count running count of partial events received (updated in-place)
	 */
	public static void printStreamingEvent(Event event, AtomicInteger count) {
		boolean isPartial = event.partial().orElse(false);
		boolean isTurnComplete = event.turnComplete().orElse(false);
		String content = event.stringifyContent();

		if (isPartial && content != null && !content.isEmpty()) {
			System.out.print(content);
			if (content.contains("\n") || content.matches(".*[.!?,;:-]$")) {
				System.out.println();
				System.out.flush();
			}
			count.incrementAndGet();
		} else if (isTurnComplete) {
			// end of streaming turn
			System.out.flush();
			System.out.println();
			System.out.println();
			System.out.println("-".repeat(70));
			System.out.printf("[Turn complete - %d partial event received]%n", count.get());
			// token usage metadata (present when the provider includes usage in the final
			// SSE chunk)
			event.usageMetadata().ifPresent(meta -> System.out.println("[Usage metadata] " + meta));
		}
	}

	/**
	 * Prints agent name, model, and prompt to stdout.
	 *
	 * @param agent  the agent to describe
	 * @param prompt the prompt about to be sent
	 */
	public static void showAgent(LlmAgent agent, String prompt) {
		System.out.println("\nAgent created: " + agent.name());
		System.out.println("Model: " + agent.model().map(Object::toString).orElse("none"));
		System.out.println("\nUser: " + prompt);
		System.out.println("-".repeat(60));
	}

	/**
	 * Prints the list of discovered and registered model providers.
	 *
	 * @param registered the providers returned by
	 *                   {@link com.github.svetanis.models.spi.ModelProviderRegistry#registerAll()}
	 */
	public static void showProviders(List<ModelProvider> registered) {
		System.out.println("Registered providers:");
		registered.forEach(p -> System.out.printf(PAT, p.getClass().getSimpleName(), p.modelPattern()));
	}
}
