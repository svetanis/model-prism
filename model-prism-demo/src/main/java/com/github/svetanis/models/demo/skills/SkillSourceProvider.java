package com.github.svetanis.models.demo.skills;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.LlmAgent;
import com.google.adk.skills.SkillSource;
import com.google.adk.tools.skills.SkillToolset;

import jakarta.inject.Provider;

/**
 * Provider class for creating the skills-demo agent.
 */
public final class SkillSourceProvider implements Provider<LlmAgent> {

	private static final String INSTRUCTION = """
			You are an expert assistant. You have access to a repository of skills.
			Use your tools to find out what skills are available.
			When asked to use a skill, you must load the skill's instructions and follow them.
			""";

	private final String model;

	public SkillSourceProvider(String model) {
		this.model = checkNotNull(model);
	}

	@Override
	public LlmAgent get() {
		SkillSource skillSource = new MathTutorSkillSource().get();
		SkillToolset skillToolset = new SkillToolset(skillSource);

		return LlmAgent.builder()//
				.name("skills-demo-agent")//
				.description("Assistant with dynamic skills")//
				.model(model)//
				.instruction(INSTRUCTION)//
				.tools(skillToolset)//
				.build();
	}
}
