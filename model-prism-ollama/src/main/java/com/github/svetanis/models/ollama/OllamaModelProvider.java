package com.github.svetanis.models.ollama;

import static java.util.Optional.empty;

import com.google.adk.models.BaseLlm;
import com.github.svetanis.models.spi.ModelProvider;
import com.github.svetanis.models.spi.OpenAiCompatibleLlm;

/**
 * ModelProvider for Ollama - fully local interface, no API key required.
 *
 * <p>Activated when {@code agent.model} starts with {@code ollama/}. Connects to a locally running
 * Ollama server (default: {@code http://localhost:11434}). Override the base URL via the {@code
 * OLLAMA_BASE_URL} environment variables.
 *
 * <p>Example: {@code agent.model=ollama/llama3}
 *
 * <p>Setup: install from <a href="https://ollama.com">ollama.com</a>, then run {@code ollama pull
 * llama3}.
 */
public class OllamaModelProvider implements ModelProvider {

  /** No-arg constructor required by {@link java.util.ServiceLoader}. */
  public OllamaModelProvider() {}

  @Override
  public String prefix() {
    return "ollama";
  }

  private static final String DEFAULT_BASE_URL = "http://localhost:11434";

  /** {@inheritDoc} */
  @Override
  public BaseLlm createFromBareModelName(String bareModelName) {
    String baseUrl = System.getenv().getOrDefault("OLLAMA_BASE_URL", DEFAULT_BASE_URL);
    String apiUrl = baseUrl.replaceAll("/$", "") + "/v1/chat/completions";
    return new OpenAiCompatibleLlm(bareModelName, apiUrl, empty());
  }
}
