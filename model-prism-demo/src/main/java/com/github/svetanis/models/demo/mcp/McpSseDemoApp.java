package com.github.svetanis.models.demo.mcp;

import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.JsonBaseModel;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;

/**
 * Demo application connecting to a remote MCP Server via Server-Sent Events
 * (SSE).
 *
 * <p>
 * Validates the ability of the ADK to establish an SSE transport connection to
 * an external MCP server (e.g., GitHub Copilot MCP) and execute tools remotely.
 */
public final class McpSseDemoApp {

	private static final String INSTRUCTION = """
			You are a helpful assistant with
			access to github tools.
			Use them to answer questions accurately.

			CRITICAL: When using the `search_repositories` tool,
			you must ONLY provide a single string parameter named `query`.
			Do NOT hallucinate parameters like `org`, `topic`, `visibility`, or `num_repo`.
			Example valid argument JSON: {"query": "org:google kubernetes"}
			""";

	private static final String PROMPT = """
			Use the `search_repositories` tool to find 3 public
			repositories in the `google` GitHub organization
			that are related to `kubernetes` or `containers`.
			Please show me the name and description of each.
			""";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);
		McpToolset mcp = mcp();
		LlmAgent agent = demoAgent(mcp);
		showAgent(agent, PROMPT);
		run(agent, PROMPT);
	}

	private static LlmAgent demoAgent(McpToolset mcp) {
		return LlmAgent.builder().name("mcp-demo-agent") //
				.description("Helpful Assistant agent") //
				.model(DemoRunner.MODEL) //
				.instruction(INSTRUCTION) //
				.tools(mcp.getTools(null).toList().blockingGet()) //
				.build();
	}

	private static McpToolset mcp(String... tools) {
		ObjectMapper mapper = JsonBaseModel.getMapper();
		StreamableHttpServerParameters params = new ServerParamsProvider().get();
		// return new McpToolset(params, mapper, asList(tools));
		return new McpToolset(params);
	}
}
