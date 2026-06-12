package com.github.svetanis.base.http.vertex;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * {@link AccessTokenSupplier} backed by Google Application Default Credentials (ADC).
 *
 * <p>This supplier lazily initialises {@link GoogleCredentials} scoped to
 * {@code cloud-platform} on first use, then caches and proactively refreshes the
 * token before it expires. All operations are thread-safe via double-checked locking
 * with a {@link ReentrantLock}.
 *
 * <p>Typical usage is indirect — obtain an instance via {@link AccessTokenSupplier#autoDetect()}.
 *
 * @see AccessTokenSupplier#autoDetect()
 */
public final class AdcAccessTokenSupplier implements AccessTokenSupplier {

  /** Number of seconds before expiry at which the token is proactively refreshed. */
  private static final long REFRESH_LEAD_TIME_SECONDS = 60;

  /** OAuth2 scopes requested when creating scoped credentials. */
  private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");

  private final ReentrantLock refreshLock = new ReentrantLock();
  private volatile GoogleCredentials credentials;

  /**
   * {@inheritDoc}
   *
   * <p>Lazily initialises credentials on first call, then returns a cached token.
   * If the cached token is within {@value #REFRESH_LEAD_TIME_SECONDS} seconds of
   * expiry, a refresh is triggered before returning.
   *
   * @throws IOException if ADC credentials cannot be located or the token refresh fails
   */
  @Override
  public String getAccessToken() throws IOException {
    ensureCredentials();
    AccessToken cached = credentials.getAccessToken();
    if (cached == null || isExpiringSoon(cached)) {
      refresh();
      cached = credentials.getAccessToken();
    }
    if (cached == null) {
      throw new IOException("Failed to obtain a Vertex access token from ADC");
    }
    return cached.getTokenValue();
  }

  /**
   * Lazily initialises {@link GoogleCredentials} via ADC with double-checked locking.
   *
   * @throws IOException if ADC credentials cannot be obtained
   */
  private void ensureCredentials() throws IOException {
    if (credentials == null) {
      refreshLock.lock();
      try {
        if (credentials == null) {
          credentials = GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
        }
      } finally {
        refreshLock.unlock();
      }
    }
  }

  /**
   * Thread-safe token refresh. Only one thread performs the actual refresh;
   * concurrent callers block until refresh completes.
   *
   * @throws IOException if the refresh request fails
   */
  private void refresh() throws IOException {
    refreshLock.lock();
    try {
      AccessToken now = credentials.getAccessToken();
      if (now == null || isExpiringSoon(now)) {
        credentials.refresh();
      }
    } finally {
      refreshLock.unlock();
    }
  }

  /**
   * Returns {@code true} if the token will expire within {@value #REFRESH_LEAD_TIME_SECONDS}
   * seconds or has no expiration time set.
   */
  private static boolean isExpiringSoon(AccessToken token) {
    return token.getExpirationTime() == null || token.getExpirationTime().toInstant().isBefore(Instant.now().plusSeconds(REFRESH_LEAD_TIME_SECONDS));
  }
}
