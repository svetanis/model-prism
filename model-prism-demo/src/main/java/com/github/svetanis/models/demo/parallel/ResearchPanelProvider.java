package com.github.svetanis.models.demo.parallel;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.ParallelAgent;
import com.google.adk.tools.GoogleSearchTool;

import jakarta.inject.Provider;

/**
 * {@link Provider} that builds a {@link ParallelAgent} containing three
 * domain-expert sub-agents: historian, scientist, and economist.
 *
 * <p>
 * Each expert runs concurrently and writes its output to a distinct
 * session-state key ({@code history_notes}, {@code science_notes},
 * {@code economics_notes}), making the results available to downstream agents
 * in the pipeline. The scientist agent uses {@code gemini-2.5-flash} with
 * Google Search for grounded answers.
 *
 * @see ReseachPipelineProvider
 */
public final class ResearchPanelProvider implements Provider<ParallelAgent> {

	private static final String HISTORIAN = """
			You are a historian.
			In 4-5 sentences give the historical context
			and the key milestones for the topic provided.
			""";

	private static final String SCIENTIST = """
			You are a scientiest.
			In 4-5 sentences explain the key scientific
			principles and current research on the topic.
			""";

	private static final String ECONOMIST = """
			You are an economist.
			In 4-5 sentences describe the economic impact
			and near-term market outlook for the topic.
			""";

	public ResearchPanelProvider(String model) {
		this.model = checkNotNull(model, "model");
	}

	private final String model;

	@Override
	public ParallelAgent get() {
		return ParallelAgent.builder() //
				.name("research-panel") //
				.description("Parallel research: historian + scientist + economist") //
				.subAgents(historian(), scientist(), economist()) //
				.build();
	}

	private LlmAgent historian() {
		return LlmAgent.builder().name("historian") //
				.description("Historical context") //
				.model(model).instruction(HISTORIAN) //
				.outputKey("history_notes").build();
	}

	private LlmAgent scientist() {
		return LlmAgent.builder().name("scientist") //
				.description("Scientific principles") //
				.model("gemini-2.5-flash").instruction(SCIENTIST) //
				.outputKey("science_notes").tools(new GoogleSearchTool()).build();
	}

	private LlmAgent economist() {
		return LlmAgent.builder().name("economist") //
				.description("Economic impact") //
				.model(model).instruction(ECONOMIST) //
				.outputKey("economics_notes").build();
	}
}
