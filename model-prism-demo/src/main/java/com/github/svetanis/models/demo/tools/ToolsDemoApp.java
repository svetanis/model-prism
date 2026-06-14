package com.github.svetanis.models.demo.tools;

import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.ArrayList;
import java.util.List;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;

/**
 * Demo application for ADK function-calling (tool use) with a model-prism
 * provider.
 *
 * <p>
 * Equips an agent with three tools ({@code getCurrentTime}, {@code getWeather},
 * {@code calculate}) and sends a prompt that requires all three to be invoked.
 * This validates the full tool-calling round-trip: request serialization with
 * tool declarations → model response with tool calls → tool execution →
 * function-response serialization → final model answer.
 *
 * @see DemoTools
 */
public final class ToolsDemoApp {

	private static final String PROMPT = """
			What time is right now?
			Also, what's the weather like in Phoenix?
			And finally, what is 42 multiplied by 137?
			""";

	private static final String INSTRUCTION = """
			You are a helpful assistant.
			Use the available tools to answer questions accurately.
			""";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);
		LlmAgent agent = demoAgent();
		showAgent(agent, PROMPT);
		run(agent, PROMPT);
	}

	private static LlmAgent demoAgent() {
		return LlmAgent.builder().name("tools-demo-agent") //
				.description("Helpful Assistant agent with tools") //
				.model(DemoRunner.MODEL) //
				.instruction(INSTRUCTION) //
				.tools(tools()) //
				.build();
	}

	private static List<BaseTool> tools() {
		List<BaseTool> tools = new ArrayList<>();
		tools.add(FunctionTool.create(DemoTools.class, "getCurrentTime"));
		tools.add(FunctionTool.create(DemoTools.class, "getWeather"));
		tools.add(FunctionTool.create(DemoTools.class, "calculate"));
		return tools;
	}
}
