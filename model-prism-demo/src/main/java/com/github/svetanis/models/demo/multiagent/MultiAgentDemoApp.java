package com.github.svetanis.models.demo.multiagent;

import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;

/**
 * Demo application for the multi-agent content-creation pipeline.
 *
 * <p>
 * Orchestrates a {@link SequentialAgent} pipeline: a researcher agent gathers
 * facts via Google Search, then a writer and critic iterate in a
 * {@link com.google.adk.agents.LoopAgent} to produce a polished article. Events
 * from all agents are printed with role-specific labels.
 *
 * @see ContentPipelineProvider
 * @see RefinementLoopProvider
 */
public final class MultiAgentDemoApp {

	private static final String TOPIC = "the future of renewable energy";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);

		SequentialAgent agent = new ContentPipelineProvider(DemoRunner.MODEL).get();
		String prompt = "Write a briefing on this topic: %s".formatted(TOPIC);
		show(prompt);
		run(agent, prompt, e -> printEvent(e));
	}

	public static void show(String prompt) {
		System.out.println("Multi-agent system Demo - researcher -> (writer <-> critic) x 2");
		System.out.println("-".repeat(70));
		System.out.println("Topic: " + TOPIC);
		System.out.println("-".repeat(70));
	}

	private static void printEvent(Event event) {
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
			System.out.println("-".repeat(60));
			String label = switch (author) {
			case "researcher" -> "[Researcher - notes complete]";
			case "writer" -> "[Writer - draft complete]";
			case "critic" -> "[Critic - feedback]";
			default -> "[" + author + " - complete]";
			};
			System.out.println(label);
			System.out.println(content);
			System.out.println("-".repeat(60));
		} else {
			System.out.printf("[%s] %s%n", author, content);
		}
	}
}
