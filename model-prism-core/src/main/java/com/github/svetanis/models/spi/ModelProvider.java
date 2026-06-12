package com.github.svetanis.models.spi;

import org.apache.commons.lang3.StringUtils;

import com.google.adk.models.BaseLlm;

/**
 * SPI (Service Provider Interface) for pluggable LLM backends in Google ADK
 * Java.
 *
 * <h2>Purpose</h2>
 *
 * <p>
 * This interface is the proposed extension point that would allow third-party
 * providers (Groq,
 * Ollama, OpenRouter, etc.) to register themselves with
 * {@link com.google.adk.models.LlmRegistry}
 * automatically - without any changes to {@code App.java} or ADK core.
 *
 * <h2>How it works</h2>
 *
 * <ol>
 * <li>A provider library implements this interface.
 * <li>It declares the implementation in {@code
 *       META-INF/services/com.github.svetanis.models.spi.ModelProvider}.
 * <li>At startup, {@link ModelProviderRegistry#registerAll()} uses {@link
 * java.util.ServiceLoader} to discover and register all providers on the
 * classpath.
 * </ol>
 *
 * <h2>Proposed location in ADK core</h2>
 *
 * <p>
 * This interface would live in {@code google-adk.jar} under
 * {@code com.github.svetanis.models.spi},
 * making it part of ADK's public API surface
 */
public interface ModelProvider {

  /**
   * Returns the provider prefix without the trailing slash, e.g.
   * {@code "groq"}.
   *
   * <p>
   * This prefix is used to automatically derive the {@link #modelPattern()} and
   * to strip
   * the prefix from the model name before delegating to
   * {@link #createFromBareModelName(String)}.
   *
   * @return the provider prefix, must not be blank
   */
  String prefix();

  /**
   * Returns the regex pattern this provider handles.
   *
   * <p>
   * The default implementation derives the pattern from {@link #prefix()}, e.g.
   * returning {@code "groq/.*"}.
   */
  default String modelPattern() {
    String prefix = prefix();
    if (StringUtils.isBlank(prefix)) {
      throw new IllegalStateException("Provider prefix cannot be blank");
    }
    return prefix + "/.*";
  }

  /**
   * Creates a {@link BaseLlm} instance for the given model name.
   *
   * <p>
   * The default implementation strips the {@link #prefix()} and trailing slash from the model name
   * and delegates
   * to {@link #createFromBareModelName(String)}.
   *
   * @param modelName the full model name, e.g.
   *                  {@code "groq/llama-3.1-8b-instant"}
   */
  default BaseLlm create(String modelName) {
    String prefixWithSlash = prefix() + "/";
    String bareModelName = modelName.startsWith(prefixWithSlash) ? modelName.substring(prefixWithSlash.length()) : modelName;
    return createFromBareModelName(bareModelName);
  }

  /**
   * Creates a {@link BaseLlm} for the given bare model identifier.
   *
   * <p>
   * The {@code bareModelName} has already had the provider prefix removed.
   * Pass it directly to the underlying API client.
   *
   * @param bareModelName the model name without prefix, e.g.
   *                      {@code "llama-3.1-8b-instant"}
   */
  BaseLlm createFromBareModelName(String bareModelName);
}
