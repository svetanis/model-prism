package com.github.svetanis.models.demo.observability;

import static com.github.svetanis.models.demo.DemoRunner.showAgent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.plugins.agentanalytics.BigQueryAgentAnalyticsPlugin;
import com.google.adk.plugins.agentanalytics.BigQueryLoggerConfig;
import com.google.adk.runner.InMemoryRunner;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

/**
 * Demo application for ADK Observability and BigQuery Analytics integration.
 *
 * <p>
 * Validates that the model-prism proxy correctly emits standardized ADK
 * observability spans and metrics, allowing the
 * {@link BigQueryAgentAnalyticsPlugin} to log inference events to BigQuery.
 */
public final class ObservabilityDemoApp {

	private static final String PROMPT = "Explain the importance of software observability in 2 sentences.";

	public static void main(String[] args) throws Exception {
		// Register model-prism providers
		ModelProviderRegistry.registerAll();

		// 1. Configure the plugins
		BigQueryAgentAnalyticsPlugin bqPlugin = buildBigQueryPlugin();

		// 2. Build the agent
		LlmAgent agent = new ObservabilityProvider(DemoRunner.MODEL).get();
		showAgent(agent, PROMPT);

		// 3. Run the agent. The plugin will intercept events and attempt to log them.
		ConsoleAnalyticsPlugin consolePlugin = new ConsoleAnalyticsPlugin();
		InMemoryRunner runner = new InMemoryRunner(agent, "observability-demo", List.of(bqPlugin, consolePlugin));
		String sessionId = UUID.randomUUID().toString();
		RunConfig config = RunConfig.builder().autoCreateSession(true).build();
		runner.runAsync("demo-user", sessionId, Content.fromParts(Part.fromText(PROMPT)), config).blockingSubscribe();

		// Ensure the plugin's background publisher is flushed before JVM exit
		bqPlugin.close().blockingAwait();
	}

	private static BigQueryAgentAnalyticsPlugin buildBigQueryPlugin() throws IOException {
		String projectId = System.getenv().getOrDefault("GOOGLE_CLOUD_PROJECT", "demo-project-id");
		BigQueryLoggerConfig bqConfig = BigQueryLoggerConfig.builder()//
				.projectId(projectId)//
				.datasetId("agent_analytics")//
				.tableName("events").build();//
		System.out.println("Configured BigQuery Analytics for project: " + projectId);
		return new BigQueryAgentAnalyticsPlugin(bqConfig);
	}
}
