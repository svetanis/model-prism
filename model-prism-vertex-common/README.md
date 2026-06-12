# model-prism-vertex-common

A shared module providing Google Application Default Credentials (ADC) logic and HTTP transport mechanisms for Vertex AI providers in Google ADK Java.

---

## Why This Exists

Model Prism has two separate Vertex AI integrations:
1. `model-prism-vertex-gemini` (uses the native `google-genai` SDK)
2. `model-prism-vertex-openai` (uses the OpenAI-compatible REST endpoint)

Both of these integrations need access to Google Cloud authentication, specifically short-lived OAuth2 bearer tokens minted from Google Application Default Credentials (ADC).

Instead of duplicating this logic or bleeding Google Auth dependencies into the `model-prism-core` module (which would force Groq or Ollama users to download Google libraries), the authentication and token refresh logic is encapsulated in this `model-prism-vertex-common` module.

## Contents

| Component | Responsibility |
|---|---|
| `AccessTokenSupplier` | Interface for retrieving an active OAuth2 bearer token. |
| `AdcAccessTokenSupplier` | Implementation that uses `GoogleCredentials.getApplicationDefault()` to retrieve and refresh tokens. |
| `EnvVarAccessTokenSupplier` | Implementation that uses a static token from the `VERTEX_ACCESS_TOKEN` environment variable. |
| `RefreshingBearerHttpClient` | An `OpenAiHttpClient` implementation that injects the dynamically refreshed token into the `Authorization: Bearer` header on every request. |

## Usage

This module is not intended to be used directly by end-users. It is an internal dependency for `model-prism-vertex-gemini` and `model-prism-vertex-openai`. End users should declare a dependency on one of those specific provider modules instead.
