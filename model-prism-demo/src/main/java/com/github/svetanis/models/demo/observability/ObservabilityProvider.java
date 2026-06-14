package com.github.svetanis.models.demo.observability;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.LlmAgent;

import jakarta.inject.Provider;

/**
 * Provider class for creating the observability-demo agent.
 */
public final class ObservabilityProvider implements Provider<LlmAgent> {

	private static final String INSTRUCTION = """
			You are a helpful assistant.
			Please answer concisely.
			""";

	private final String model;

	public ObservabilityProvider(String model) {
		this.model = checkNotNull(model, "model");
	}

	@Override
	public LlmAgent get() {
		return LlmAgent.builder()//
				.name("observability-demo-agent")//
				.description("Assistant with BigQuery Analytics enabled")//
				.model(model)//
				.instruction(INSTRUCTION)//
				.build();
	}
}
