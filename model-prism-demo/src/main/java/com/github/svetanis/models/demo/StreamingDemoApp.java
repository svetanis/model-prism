package com.github.svetanis.models.demo;

import static com.github.svetanis.models.demo.DemoRunner.printStreamingEvent;
import static com.github.svetanis.models.demo.DemoRunner.runStreaming;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;

/**
 * SSE streaming demo showing token-by-token output from a model-prism provider.
 *
 * <p>
 * Runs a creative writing agent in
 * {@link com.google.adk.agents.RunConfig.StreamingMode#SSE} mode. Each partial
 * token is printed as it arrives, followed by a summary of the total partial
 * events received. This validates the streaming path through
 * {@link com.github.svetanis.models.spi.OpenAiCompatibleLlm} and
 * {@link com.github.svetanis.base.serializer.DefaultOpenAiMessageSerializer#processStreamLines}.
 *
 * @see DemoRunner#runStreaming
 * @see DemoRunner#printStreamingEvent
 */
public final class StreamingDemoApp {

	private static final String INSTRUCTION = """
			You are a creative writing assistant.
			Be concise;
			""";

	private static final String PROMPT = """
			Write a short poem (4-6 lines) about the joy
			of asynchronous programming.
			""";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);
		LlmAgent agent = demoAgent();
		showAgent(agent, PROMPT);
		System.out.println("Streaming response (each token printed as it arrives):");
		var count = new AtomicInteger(0);
		runStreaming(agent, PROMPT, event -> printStreamingEvent(event, count));
	}

	private static LlmAgent demoAgent() {
		return LlmAgent.builder().name("streaming-demo-agent") //
				.description("Writing Assistant agent") //
				.model(DemoRunner.MODEL).instruction(INSTRUCTION) //
				.build();
	}
}
