package com.github.svetanis.models.demo.hitl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;

import jakarta.inject.Provider;

/**
 * Provider class for creating the hitl-demo agent.
 */
public final class HitlProvider implements Provider<LlmAgent> {

	private static final String INSTRUCTION = """
			You are an automated DevBot. You can merge Pull Requests automatically.
			However, if a PR modifies core build files like pom.xml or contains
			sensitive security changes, you MUST request Human approval using the
			request_pr_approval tool before merging.
			If the human rejects the PR, you MUST use the close_pr tool to close the PR.

			CRITICAL: NEVER call merge_pr or close_pr in the same turn as request_pr_approval!
			You must WAIT for the user to explicitly reply with 'APPROVED' before using merge_pr.
			""";

	private final String model;

	public HitlProvider(String model) {
		this.model = checkNotNull(model, "model");
	}

	@Override
	public LlmAgent get() {
		HitlTools tools = new HitlTools();
		// Create a FunctionTool wrapping the HITL method
		FunctionTool request = FunctionTool.create(tools, "requestPrApproval");
		FunctionTool merge = FunctionTool.create(tools, "mergePr");
		FunctionTool close = FunctionTool.create(tools, "closePr");
		return LlmAgent.builder()//
				.name("devbot-agent")//
				.description("DevBot that escalates PRs for manual review")//
				.model(model)//
				.instruction(INSTRUCTION)//
				.tools(request, merge, close)//
				.build();
	}
}
