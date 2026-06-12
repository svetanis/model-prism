package com.github.svetanis.models.spi;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;

import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.models.chat.ChatCompletionsHttpClient;
import com.google.genai.types.HttpOptions;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Native Google ADK implementation of an OpenAI-compatible LLM.
 * 
 * <p>Unlike {@link OpenAiCompatibleLlm} which uses custom HTTP and JSON serialization
 * adapters, this class utilizes the native {@link ChatCompletionsHttpClient} provided
 * by ADK 1.4.0+, which merges both HTTP transport and JSON mapping into a single
 * highly optimized pipeline.
 * 
 * <p><strong>WARNING:</strong> This class is provided as an example and is NOT wired by default
 * because the native ADK JSON serialization engine currently outputs uppercase JSON Schema types 
 * (e.g., {@code "type": "STRING"}) when processing tools. Strict OpenAI-compatible providers 
 * like Groq strictly enforce the JSON Schema standard (which requires lowercase types like 
 * {@code "string"}) and will reject the request with an HTTP 400. Our custom 
 * {@link OpenAiCompatibleLlm} implements a fix for this behavior.
 */
public class AdkNativeOpenAiLlm extends BaseLlm {

  private final ChatCompletionsHttpClient client;

  /**
   * Creates a new native ADK LLM adapter.
   * 
   * @param modelName the bare model name (e.g. {@code "llama-3.1-8b-instant"})
   * @param apiUrl    the full URL of the chat-completions endpoint
   * @param apiKey    optional API key; if empty, no {@code Authorization} header is sent
   */
  public AdkNativeOpenAiLlm(String modelName, String apiUrl, Optional<String> apiKey) {
    super(checkNotNull(modelName, "modelName"));
    checkNotNull(apiUrl, "apiUrl");

    // ChatCompletionsHttpClient automatically appends "/chat/completions" internally
    String baseUrl = apiUrl;
    if (baseUrl.endsWith("/chat/completions")) {
      baseUrl = baseUrl.substring(0, baseUrl.length() - "/chat/completions".length());
    }

    HttpOptions.Builder optionsBuilder = HttpOptions.builder().baseUrl(baseUrl);
    apiKey.ifPresent(key -> optionsBuilder.headers(Map.of("Authorization", "Bearer " + key)));
    
    this.client = new ChatCompletionsHttpClient(optionsBuilder.build());
  }

  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest request, boolean stream) {
    return client.complete(request, stream);
  }

  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    throw new UnsupportedOperationException(
        getClass().getSimpleName() + " does not support live streaming connections");
  }
}
