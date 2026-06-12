package com.github.svetanis.models.openrouter;

import com.google.adk.models.BaseLlm;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.OpenAiCompatibleLlm;
import java.util.Optional;

/**
 * ModelProvider for the OpenRouter - aggregates many models through one OpenAI-compatible API.
 *
 * <p>Activated when {@code agent.model} starts with {@code openrouter/}. Reads the API key from the
 * {@code OPENROUTER_API_KEY} environment variable.
 *
 * <p>Example: {@code agent.model=openrouter/auto}
 *
 * <p>Free tier models (suffix {@code :free}) require no credits. See <a
 * href="https://openrouter.ai/models">openrouter.ai/models</a> for the full list.
 */
public class OpenRouterModelProvider implements ModelProvider {

  /** No-arg constructor required by {@link java.util.ServiceLoader}. */
  public OpenRouterModelProvider() {}

  @Override
  public String prefix() {
    return "openrouter";
  }

  private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

  /** {@inheritDoc} */
  @Override
  public BaseLlm createFromBareModelName(String bareModelName) {
    Optional<String> apiKey = Optional.ofNullable(System.getenv("OPENROUTER_API_KEY"));
    return new OpenAiCompatibleLlm(bareModelName, API_URL, apiKey);
  }
}
