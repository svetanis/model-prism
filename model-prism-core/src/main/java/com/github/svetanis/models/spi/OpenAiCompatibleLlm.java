package com.github.svetanis.models.spi;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.stream.Stream;

import com.github.svetanis.base.http.DefaultOpenAiHttpClient;
import com.github.svetanis.base.http.OpenAiHttpClient;
import com.github.svetanis.base.serializer.DefaultOpenAiMessageSerializer;
import com.github.svetanis.base.serializer.OpenAiMessageSerializer;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.BaseLlmConnection;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Base class for any OpenAI-compatible chat completion API (Groq, Ollama, OpenRouter, Together AI,
 * Fireworks, ...).
 *
 * <p>Orchestrates two collaborators:
 *
 * <ul>
 *   <li>{@link OpenAiHttpClient} - handles HTTP transport
 *   <li>{@link OpenAiMessageSerializer} - handles JSON serialization / deserialization
 * </ul>
 *
 * <p>A new provider needs only a one-liner subclass:
 *
 * <pre>
 * public class GroqLlm extends OpenAiCompatibleLlm {
 *   public GroqLlm(String modelName, String apiKey) {
 *     super(modelName, "https://api.groq.com/openai/v1/chat/completions", apiKey);
 *   }
 * }
 * </pre>
 *
 * <p>The {@code protected} injection constructor accepts custom collaborators, which is useful for
 * subclasses and unit tests.
 */
public class OpenAiCompatibleLlm extends BaseLlm {

  /**
   * Convenience constructor that wires in default collaborators.
   *
   * @param modelName the bare model name (e.g. {@code "llama-3.1-8b-instant"})
   * @param apiUrl    the full URL of the chat-completions endpoint
   * @param apiKey    optional API key; if empty, no {@code Authorization} header is sent
   */
  public OpenAiCompatibleLlm(String modelName, String apiUrl, Optional<String> apiKey) {
    this(
        modelName,
        new DefaultOpenAiHttpClient(apiUrl, apiKey),
        new DefaultOpenAiMessageSerializer());
  }

  /**
   * Injection constructor accepting custom collaborators, useful for subclasses
   * and unit tests.
   *
   * @param modelName  the bare model name
   * @param httpClient the HTTP transport to use
   * @param serializer the message serializer/deserializer to use
   */
  public OpenAiCompatibleLlm(
      String modelName, OpenAiHttpClient httpClient, OpenAiMessageSerializer serializer) {
    super(checkNotNull(modelName, "modelName"));
    this.httpClient = checkNotNull(httpClient, "httpClient");
    this.serializer = checkNotNull(serializer, "serializer");
  }

  private final OpenAiHttpClient httpClient;
  private final OpenAiMessageSerializer serializer;

  /**
   * {@inheritDoc}
   *
   * <p>When {@code stream} is {@code true}, returns a {@link Flowable} that emits
   * partial text tokens and a final assembled response. When {@code false}, returns
   * a single-element {@link Flowable} with the complete response.
   */
  @Override
  public Flowable<LlmResponse> generateContent(LlmRequest request, boolean stream) {
    if (stream) {
      return streamContent(request);
    }
    return Flowable.fromCallable(
        () -> {
          String requestBody = serializer.serializeRequest(request, model(), false);
          return serializer.deserializeResponse(httpClient.post(requestBody));
        });
  }

  /**
   * Streams a request via SSE and emits partial/final responses.
   */
  private Flowable<LlmResponse> streamContent(LlmRequest request) {
    return Flowable.create(
        emitter -> {
          try {
            String requestBody = serializer.serializeRequest(request, model(), true);
            try (Stream<String> lines = httpClient.postStream(requestBody)) {
              serializer.processStreamLines(lines, emitter);
            }
            emitter.onComplete();
          } catch (Exception e) {
            emitter.onError(e);
          }
        },
        BackpressureStrategy.BUFFER);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Live (bidirectional) streaming is not supported by OpenAI-compatible APIs.
   *
   * @throws UnsupportedOperationException always
   */
  @Override
  public BaseLlmConnection connect(LlmRequest llmRequest) {
    throw new UnsupportedOperationException(
        getClass().getSimpleName() + " does not support live streaming connections");
  }
}
