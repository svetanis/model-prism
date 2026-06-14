package com.github.svetanis.models.demo.parallel;

import static com.github.svetanis.models.demo.DemoRunner.printEvent;
import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

import java.util.List;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;

/**
 * Demo application for parallel agent execution with a research pipeline.
 *
 * <p>
 * Launches three expert agents (historian, scientist, economist) in parallel
 * via a {@link com.google.adk.agents.ParallelAgent}, then feeds their combined
 * notes into a synthesizer agent that produces a unified briefing. Optionally
 * filters output to show only the final synthesizer response (controlled by the
 * {@code filter.event} system property).
 *
 * @see ReseachPipelineProvider
 * @see ResearchPanelProvider
 */
public final class ParallelAgentDemoApp {

	private static final String TOPIC = "the global transition to renewable energy";
	private static final boolean doFilter = parseBoolean(getProperty("filter.event", "false"));

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);

		SequentialAgent agent = new ReseachPipelineProvider(DemoRunner.MODEL).get();
		String prompt = "Write a briefing on this topic: %s".formatted(TOPIC);
		show(prompt);

		if (doFilter) {
			run(agent, prompt, e -> filter(e));
		} else {
			run(agent, prompt, e -> printEvent(e));
		}
	}

	private static void filter(Event e) {
		if ("synthesizer".equals(e.author())) {
			printEvent(e);
		}
	}

	public static void show(String prompt) {
		System.out.println("Multi-agent system Demo - filtered output (only synthesizer output shown)");
		System.out.println("Topic: " + TOPIC);
		System.out.println("-".repeat(60));
	}
}
