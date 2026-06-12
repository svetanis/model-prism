package com.github.svetanis.base.http.vertex;

import com.google.api.client.util.Preconditions;

/**
 * {@link AccessTokenSupplier} that returns a fixed access token supplied via the
 * {@code VERTEX_ACCESS_TOKEN} environment variable.
 *
 * <p>This supplier performs no refresh — the token is assumed to be valid for the
 * lifetime of the process. It is most useful for local development, CI pipelines,
 * or short-lived jobs where a pre-generated token is injected into the environment.
 *
 * <p>Typical usage is indirect — obtain an instance via {@link AccessTokenSupplier#autoDetect()}.
 *
 * @see AccessTokenSupplier#autoDetect()
 */
public class EnvVarAccessTokenSupplier implements AccessTokenSupplier {

  private final String token;

  /**
   * Creates a supplier that always returns the given static token.
   *
   * @param token the pre-generated access token; must not be {@code null}
   * @throws NullPointerException if {@code token} is {@code null}
   */
  public EnvVarAccessTokenSupplier(String token) {
    this.token = Preconditions.checkNotNull(token, "token");
  }

  /** {@inheritDoc} */
  @Override
  public String getAccessToken() {
    return token;
  }
}
