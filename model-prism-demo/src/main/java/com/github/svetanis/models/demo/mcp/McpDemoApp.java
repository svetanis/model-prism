package com.github.svetanis.models.demo.mcp;

import static com.github.svetanis.models.demo.DemoRunner.run;
import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.List;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;

/**
 * Demo application for MCP (Model Context Protocol) integration via stdio
 * transport using the newer asynchronous toolset.
 *
 * <p>
 * Connects a model-prism-backed agent to an MCP-compliant tool server
 * ({@code @modelcontextprotocol/server-filesystem}) launched as a subprocess
 * via {@code npx}. This showcases the
 * {@link com.google.adk.tools.mcp.McpAsyncToolset} which provides better
 * non-blocking I/O over MCP.
 *
 * @see com.google.adk.tools.mcp.McpAsyncToolset
 * @see com.google.adk.tools.mcp.StdioServerParameters
 */
public final class McpDemoApp {

	private static final String PROMPT = """
			List the items in the root directory.
			How many files vs directories are there?
			""";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);
		String root = args.length > 0 ? args[0] : System.getProperty("user.home");
		System.out.println("MCP Async Demo - filesystem root: " + root);
		LlmAgent asyncAgent = new McpProvider(DemoRunner.MODEL, root, true).get();
		showAgent(asyncAgent, PROMPT);
		run(asyncAgent, PROMPT);

		System.out.println("\n------------------------------------------------\n");

		System.out.println("MCP Sync Demo - filesystem root: " + root);
		LlmAgent syncAgent = new McpProvider(DemoRunner.MODEL, root, false).get();
		showAgent(syncAgent, PROMPT);
		run(syncAgent, PROMPT);
	}
}
