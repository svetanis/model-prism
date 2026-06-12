package com.github.svetanis.models.groq;

import static java.util.Optional.ofNullable;

import com.google.adk.models.BaseLlm;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.OpenAiCompatibleLlm;
import java.util.Optional;

/**
 * ModelProvider for the Groq API.
 *
 * <p>Activated when {@code agent.model} starts with {@code groq/}. Reads the API key from the
 * {@code GROQ_API_KEY} environment variable.
 *
 * <p>Example: {@code agent.model=groq/llama-3.1-8b-instant}
 *
 * <p>Free tier available at <a href="https://console.groq.com">console.groq.com</a>.
 *
 * <p>Models with tool calling support: {@code llama-3.1-8b-instant}, {@code mixtral-8x7b-32768}
 */
public class GroqModelProvider implements ModelProvider {

  /** No-arg constructor required by {@link java.util.ServiceLoader}. */
  public GroqModelProvider() {}

  @Override
  public String prefix() {
    return "groq";
  }

  private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

  /** {@inheritDoc} */
  @Override
  public BaseLlm createFromBareModelName(String bareModelName) {
    Optional<String> apiKey = ofNullable(System.getenv("GROQ_API_KEY"));
    return new OpenAiCompatibleLlm(bareModelName, API_URL, apiKey);
  }
}
