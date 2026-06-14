package com.github.svetanis.models.demo.structured;

import static com.github.svetanis.models.demo.DemoRunner.showAgent;
import static com.github.svetanis.models.demo.DemoRunner.showProviders;

import java.util.List;

import com.github.svetanis.models.demo.DemoRunner;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.agents.LlmAgent;
import com.google.genai.types.Schema;

// mvn exec:java -Dexec.mainClass=com.github.svetanis.models.demo.structured.StructuredDemoApp

/**
 * Demo application for structured (schema-constrained) output from a
 * model-prism provider.
 *
 * <p>
 * Feeds a free-text movie description to an agent configured with a JSON output
 * schema (title, director, year, genre, summary). The LLM response is
 * constrained to the schema, producing machine-readable structured data from
 * unstructured input.
 *
 * @see MovieExtractorProvider
 * @see MovieSchemaProvider
 */
public final class StructuredOutputDemoApp {

	private static final String PROMPT = """
			Inception, released in 2010 and directed by Christoper Nolan,
			is a mind-bending science-fiction thriller about a skilled
			thief who steals secrets from people's dreams and is offered
			a chance to have his criminal record erased in exchange for
			planting an idea in a target's mind.
			""";

	public static void main(String[] args) {
		// One call - discovers and registers ALL providers on the classpath
		List<ModelProvider> registered = ModelProviderRegistry.registerAll();
		showProviders(registered);

		Schema schema = new MovieSchemaProvider().get();
		LlmAgent agent = new MovieExtractorProvider(DemoRunner.MODEL, schema).get();
		System.out.println("Structured Output Demo - extract typed facts from free text");
		showAgent(agent, PROMPT);
		System.out.println("Expected output: JSON with title, director, year, genre, summary");
		DemoRunner.run(agent, PROMPT);
	}
}
