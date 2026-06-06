package com.github.svetanis.models.vertex.openai;

import java.net.http.HttpClient;

import com.github.svetanis.base.http.OpenAiHttpClient;
import com.github.svetanis.base.http.vertex.AccessTokenSupplier;
import com.github.svetanis.base.http.vertex.RefreshingBearerHttpClient;
import com.github.svetanis.base.serializer.DefaultOpenAiMessageSerializer;
import com.github.svetanis.base.serializer.OpenAiMessageSerializer;
import com.github.svetanis.models.spi.OpenAiCompatibleLlm;
import com.github.svetanis.models.spi.prefix.AbstractPrefixAwareModelProvider;
import com.google.adk.models.BaseLlm;

/**
 * ModelProvider for Vertex AI via Google Cloud's OpenAI-compatible chat-completions endpoint.
 * 
 * <h2>Why this exists alongside {@code provider-vertex-gemini}?</h2>
 * <p>
 * Vertex exposes two surfaces for Gemini and partner models (Llama, Claude, ...):
 * <ol>
 * <li>The native google-genai SDK - used by {@code provider-vertex-gemini}.</li>
 * <li>An OpenAI-compatible REST endpoint - used here.</li>
 * </ol>
 * 
 * <p>
 * The OpenAI-compatible surface is the right pick when:
 * <ul>
 * <li>The caller already speaks the OpenAI wire format and you want to point it at Vertex with a base URL change (e.g.
 * the {@code spring-ai-gateway} pass-through path).</li>
 * <li>You need a partner mode (Llama, Mistral, Anthropic-on-Vertex) without pulling in a model-family-specific
 * SDK.</li>
 * <li>You want a single code path that works against Groq, OpenRouter, Ollama, <em>and</em> Vertex - useful for
 * cross-provider eval suites.</li>
 * </ul>
 * 
 * <h2>Activation</h2>
 * <p>
 * Any model string starting with {@code vertex-openai/}. The prefix is stripped; the remainder is sent to Vertex as the
 * {@code model} field. Vertex's OpenAI surface uses vendor-namespaced model ids:
 * <ul>
 * <li>{@code vertex-openai/gemini-2.5-flash} -> {@code gemini-2.5-flash}</li>
 * <li>{@code vertex-openai/gemini-2.5-pro} -> {@code gemini-2.5-pro}</li>
 * </ul>
 * 
 * <h2>Endpoint URL</h2>
 * <p>
 * {@code https://{LOCATION}-aiplatform.googleapis.com/v1/projects/{PROJECT}/locations/{LOCATION}/endpoint
 * Project comes from {@code GOOGLE_CLOUD_PROJECT}; location from {@code GOOGLE_CLOUD_LOCATION}
 * (default {@code us-central1}
 * 
 * 
<h2>Authentication</h2>
 * 
<p>
Delegated to {@link AccessTokenSupplier#autoDetect()}. See that interface for the env-var-vs-ADC resolution rule.
 */

public class VertexOpenAiModelProvider extends AbstractPrefixAwareModelProvider {

  private static final String DEFAULT_LOCATION = "us-central1";
  private static final String BASE_URL = "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s";
  private static final String CHATS = "/endpoints/openapi/chat/completions";

  public VertexOpenAiModelProvider() {
    super("vertex-openai/");
  }

  @Override
  protected BaseLlm createFromBareModelName(String bareModelName) {
    String project = requireEnv("GOOGLE_CLOUD_PROJECT");
    String location = System.getenv().getOrDefault("GOOGLE_CLOUD_LOCATION", DEFAULT_LOCATION);
    String apiUrl = String.format(BASE_URL + CHATS, location, project, location);
    AccessTokenSupplier tokens = AccessTokenSupplier.autoDetect();
    HttpClient client = HttpClient.newBuilder().build();
    OpenAiMessageSerializer json = new DefaultOpenAiMessageSerializer();
    OpenAiHttpClient http = new RefreshingBearerHttpClient(client, apiUrl, tokens);
    return new OpenAiCompatibleLlm(bareModelName, http, json);
  }

  private static String requireEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      String msg = "Environment variable %s is required by vertex-openai provider";
      throw new IllegalStateException(msg.formatted(name));
    }
    return value;
  }
}
