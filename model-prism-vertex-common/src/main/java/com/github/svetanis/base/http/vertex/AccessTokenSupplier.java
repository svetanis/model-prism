package com.github.svetanis.base.http.vertex;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * Abstraction for supplying OAuth2 access tokens used to authenticate requests
 * to Google Cloud Vertex AI endpoints.
 *
 * <p>
 * Implementations handle different token-acquisition strategies (environment
 * variable, Application Default Credentials, etc.). Use {@link #autoDetect()}
 * to obtain the most appropriate supplier for the current runtime environment.
 *
 * @see AdcAccessTokenSupplier
 * @see EnvVarAccessTokenSupplier
 */
public interface AccessTokenSupplier {

  /**
   * Returns a valid OAuth2 access token string.
   *
   * <p>
   * Implementations may cache and refresh tokens transparently.
   *
   * @return a bearer token suitable for the {@code Authorization} header
   * @throws IOException if a token cannot be obtained (e.g. credentials are
   *                     missing)
   */
  String getAccessToken() throws IOException;

  /**
   * Auto-detects the best token supplier for the current environment.
   *
   * <p>
   * Resolution order:
   * <ol>
   * <li>If the {@code VERTEX_ACCESS_TOKEN} environment variable is set and
   * non-blank,
   * returns an {@link EnvVarAccessTokenSupplier} backed by that static
   * token.</li>
   * <li>Otherwise, returns an {@link AdcAccessTokenSupplier} that uses Google
   * Application Default Credentials (ADC) with automatic refresh.</li>
   * </ol>
   *
   * @return a ready-to-use {@code AccessTokenSupplier}
   */
  static AccessTokenSupplier autoDetect() {
    String envToken = System.getenv("VERTEX_ACCESS_TOKEN");
    if (StringUtils.isNotBlank(envToken)) {
      return new EnvVarAccessTokenSupplier(envToken);
    }
    return new AdcAccessTokenSupplier();
  }

}
