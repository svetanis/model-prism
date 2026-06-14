package com.github.svetanis.models.demo.mcp;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.BaseToolset;
import com.google.adk.tools.mcp.McpAsyncToolset;
import com.google.adk.tools.mcp.McpToolset;

import io.modelcontextprotocol.client.transport.ServerParameters;
import jakarta.inject.Provider;

/**
 * Provider class for creating the MCP demo agent.
 */
public final class McpProvider implements Provider<LlmAgent> {

	private static final String INSTRUCTION = """
			You are a helpful assistant with
			access to filesystem tools.
			Use them to answer questions accurately.
			""";

	private final String model;
	private final String root;
	private final boolean useAsync;

	public McpProvider(String model, String root, boolean useAsync) {
		this.model = checkNotNull(model, "model");
		this.root = checkNotNull(root, "root");
		this.useAsync = useAsync;
	}

	@Override
	public LlmAgent get() {
		BaseToolset tools = toolset(root);
		return LlmAgent.builder()//
				.name("mcp-demo-agent")//
				.description("Helpful Assistant agent via MCP")//
				.model(model)//
				.instruction(INSTRUCTION)//
				.tools(tools.getTools(null).toList().blockingGet())//
				.build();
	}

	private BaseToolset toolset(String root) {
		ServerParameters params = McpFilesystemServer.params(root);
		if (useAsync) {
			return McpAsyncToolset.builder().connectionParams(params).build();
		} else {
			return new McpToolset(params);
		}
	}
}
