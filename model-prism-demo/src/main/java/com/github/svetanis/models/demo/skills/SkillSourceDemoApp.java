package com.github.svetanis.models.demo.skills;

import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;

/**
 * Demo application for SkillSource integration and Dynamic Tool Loading.
 *
 * <p>
 * Validates that the model-prism proxy correctly translates and handles
 * dynamically loaded tools such as ListSkillsTool, LoadSkillTool, and
 * LoadSkillResourceTool.
 */
public final class SkillSourceDemoApp {

	private static final String PROMPT = """
			What skills do you have available?

			Please load the 'math-tutor' skill and use it to teach me how to
			calculate the area of a circle.
			""";

	public static void main(String[] args) {
		// Register model-prism providers
		ModelProviderRegistry.registerAll();

		// Build the agent using the provider
		LlmAgent agent = new SkillSourceProvider(DemoRunner.MODEL).get();

		showAgent(agent, PROMPT);
		run(agent, PROMPT);
	}
}
