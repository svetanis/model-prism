package com.github.svetanis.models.demo.hitl;

import static com.github.svetanis.models.demo.DemoRunner.printEvent;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;

import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.EventActions;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

/**
 * Demo application for Human-in-the-Loop (HITL) and Sub-agent Escalation.
 *
 * <p>
 * Validates that when a tool returns an {@link EventActions} with
 * {@code escalate(true)}, the agent halts execution and bubbles the escalation
 * up, preserving state.
 */
public final class HitlEscalationDemoApp {

	private static final String PROMPT = """
			Please review and merge PR #1042.
			It updates the dependency versions in pom.xml
			and fixes a critical security vulnerability
			in the authentication module.
			""";

	public static void main(String[] args) {
		// Register model-prism providers
		ModelProviderRegistry.registerAll();

		// Build the agent using the provider
		LlmAgent agent = new HitlProvider(DemoRunner.MODEL).get();

		System.out.println("=== Automated PR Merger System ===");
		showAgent(agent, PROMPT);

		// Running the agent should result in an event containing `actions().escalate()
		// == true`
		System.out.println("Executing agent. Expecting an escalation event...");

		InMemoryRunner runner = new InMemoryRunner(agent);
		String sessionId = UUID.randomUUID().toString();
		RunConfig config = RunConfig.builder().autoCreateSession(true).build();
		AtomicBoolean needsApproval = new AtomicBoolean(false);

		runner.runAsync("demo-user", sessionId, Content.fromParts(Part.fromText(PROMPT)), config)
				.blockingForEach(event -> {
					printEvent(event);
					EventActions actions = event.actions();
					if (actions != null && actions.escalate().orElse(false)) {
						needsApproval.set(true);
					}
					// Check if any function response contains an escalation signal
					event.functionResponses().forEach(fr -> {
						if (fr.response().isPresent()) {
							Object resp = fr.response().get();
							if (resp.toString().contains("escalate=true")
									|| fr.name().orElse("").equals("request_pr_approval")) {
								needsApproval.set(true);
							}
						}
					});
				});

		if (needsApproval.get()) {
			System.out.print("\n>>> MANUAL OVERRIDE REQUIRED. Type 'APPROVED' or 'REJECTED': ");
			try (Scanner scanner = new Scanner(System.in)) {
				if (scanner.hasNextLine()) {
					String userInput = scanner.nextLine();
					System.out.println("\nSending user response back to agent: " + userInput);
					System.out.println("-".repeat(70));
					runner.runAsync("demo-user", sessionId, Content.fromParts(Part.fromText(userInput)), config)
							.blockingForEach(event -> printEvent(event));
				}
			}
		}
	}
}
