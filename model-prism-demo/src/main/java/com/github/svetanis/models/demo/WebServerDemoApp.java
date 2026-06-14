package com.github.svetanis.models.demo;

import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.List;

import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;
import com.google.adk.web.AdkWebServer;

/**
 * Web server demo launching the ADK dev UI with a model-prism-backed agent.
 *
 * <p>
 * Uses {@link com.google.adk.web.AdkWebServer#start} to start the built-in
 * development web server, providing a browser-based chat UI for interacting
 * with the agent. All model-prism providers are registered before the server
 * starts.
 *
 * @see DemoRunner#showProviders
 */
public final class WebServerDemoApp {

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);
		LlmAgent agent = demoAgent();
		AdkWebServer.start(agent);
	}

	private static LlmAgent demoAgent() {
		return LlmAgent.builder().name("demo-agent") //
				.description("Helpful Assistant agent") //
				.model(DemoRunner.MODEL).instruction("You are a helpful assistant") //
				.build();
	}
}
