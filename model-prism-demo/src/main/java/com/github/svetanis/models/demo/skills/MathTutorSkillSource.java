package com.github.svetanis.models.demo.skills;

import com.google.adk.skills.Frontmatter;
import com.google.adk.skills.InMemorySkillSource;
import com.google.adk.skills.SkillSource;

import jakarta.inject.Provider;

/**
 * Provider class for the Math Tutor skill source.
 */
public final class MathTutorSkillSource implements Provider<SkillSource> {

	private static final String NAME = "math-tutor";
	private static final String DESCRIPTION = """
			A skill that provides mathematical tutoring instructions.
			""";
	private static final String INSTRUCTIONS = """
			When acting as a math tutor, always explain the formulas step by step, and always
			provide a small ASCII art diagram of the shape you are explaining.
			""";

	public MathTutorSkillSource() {
	}

	@Override
	public SkillSource get() {
		return InMemorySkillSource.builder()//
				.skill(NAME)//
				.frontmatter(frontmatter())//
				.instructions(INSTRUCTIONS)//
				.build();
	}

	private Frontmatter frontmatter() {
		return Frontmatter.builder()//
				.name(NAME)//
				.description(DESCRIPTION)//
				.build();
	}
}
