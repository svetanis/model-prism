package com.github.svetanis.models.vertex.gemini;

import com.github.svetanis.models.spi.prefix.AbstractPrefixAwareModelProvider;
import com.google.adk.models.BaseLlm;
import com.google.adk.models.Gemini;
import com.google.adk.models.VertexCredentials;

/**
 * ModelProvider for Vertex AI Gemini using the <strong>native google-genai SDK</strong>.
 * 
 * <h2>Why a dedicated provider when ADK already supports Gemini?</h2>
 * <p>
 * ADK's built-in {@link Gemini} class transparently routes to either the Gemini Developer API or to Vertex AI depending
 * on environment variables. That implicit routing is convenient but invisible in agent core - readers of
 * {@code agent.model = "gemini-2.5-flash"} cannot tell whether the call hits Vertex or the consumer Gemini API.
 * 
 * <p>
 * This provider makes the choice explicit and discoverable. Model strings of the form {@code vertex-gemini/<model-id>}
 * unambiguously route through Vertex AI; they appear in logs, traces, and configuration with provenance baked in.
 * 
 * <h2>Activation</h2>
 * <p>
 * Any model string starting with {@code vertex-gemini/}. The prefix is stripped before being passed to the underlying
 * {@link Gemini} constructor:
 * <ul>
 * <li>{@code vertex-gemini/gemini-2.5-flash} -> {@code gemini-2.5-flash}</li>
 * <li>{@code vertex-gemini/gemini-2.5-pro} -> {@code gemini-2.5-pro}</li>
 * </ul>
 * 
 * <h2>Required environment</h2>
 * <ul>
 * <li>{@code GOOGLE_CLOUD_PROJECT} - Vertex AI-enabled GCP project id</li>
 * <li>{@code GOOGLE_CLOUD_LOCATION} - region, e.g. {@code us-central-1}</li>
 * <li>{@code GOOGLE_APPLICATION_CREDENTIALS} - path to service-account key <em>or</em> ambient ADC (Cloud Run, GCE,
 * {@code gcloud auth application-default login}</li>
 * <li>{@code GOOGLE_GENAI_USE_VERTEXAI=true} - forced to {@code true} by this provider via {@link System#setProperty}
 * as a defence against accidental routing through the Developer API when only {@code GOOGLE_API_KEY} happens to also be
 * set.</li>
 * </ul>
 * 
 * <h2>Why not also expose project/location knobs?</h2>
 * <p>
 * Configuration belongs to the deployment, not the model string. Mixing infra into the model identifier (e.g.
 * {@code vertex-gemini/us-central1/my-proj/gemini-2.5-pro}) would leak environment into agent code and prevent the same
 * agent definition from being promoted between dev, staging, and prod.
 * 
 * <h2>Streaming, tools, and structured output</h2>
 * <p>
 * All delegated to the underlying ADK {@link Gemini} implementation. This provider adds no behaviour of its own beyond
 * registration and prefix handling.
 */

public class VertexGeminiModelProvider extends AbstractPrefixAwareModelProvider {

  public VertexGeminiModelProvider() {
    super("vertex-gemini/");
    // Defensive: ensure the google-genai SDK selects the Vertex transport even
    // if the host environment has GOOGLE_API_KEY set. System properties take
    // precedence over env vars for the SDK's configuration lookup.
    System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "true");
  }

  @Override
  protected BaseLlm createFromBareModelName(String bareModelName) {
    // ADK's Gemini class reads project/location/credentials from the environment.
    // No constructor-level overrides are exposed here - see the class-level Javadoc
    // for the rationale. The explicit (VertexCredentials) null disambiguates from
    // the Gemini(String, String) constructor overload; passing null here means
    // "use ambient ADC", which is exactly what we want.
    return new Gemini(bareModelName, (VertexCredentials) null);
  }
}
