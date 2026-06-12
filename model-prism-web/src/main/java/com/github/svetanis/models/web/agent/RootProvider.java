package com.github.svetanis.models.web.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.tools.AgentTool;
import com.google.common.base.Preconditions;
import jakarta.inject.Provider;

public class RootProvider implements Provider<LlmAgent> {

  private static final String ROOT_AGENT_NAME = "root";
  private static final String ROOT_AGENT_DESCRIPTION = """
  Root agent that handles user requests and invokes the research pipeline
  """;

  private static final String ROOT_AGENT_INSTRUCTION = """
  You are a helpful assistant. When the user asks for research on a topic, use the pipeline tool to gather and synthesize the information.
  """;

  private final String model;
  private final SequentialAgent researchPipeline;

  public RootProvider(String model, SequentialAgent researchPipeline) {
    this.model = Preconditions.checkNotNull(model);
    this.researchPipeline = Preconditions.checkNotNull(researchPipeline);
  }

  @Override
  public LlmAgent get() {
    return LlmAgent.builder()
        .name(ROOT_AGENT_NAME)
        .description(ROOT_AGENT_DESCRIPTION)
        .model(model)
        .instruction(ROOT_AGENT_INSTRUCTION)
        .tools(AgentTool.create(researchPipeline))
        .build();
  }
}
