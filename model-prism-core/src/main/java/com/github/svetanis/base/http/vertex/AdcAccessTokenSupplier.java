package com.github.svetanis.base.http.vertex;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

public final class AdcAccessTokenSupplier implements AccessTokenSupplier {

  private static final long REFRESH_LEAD_TIME_SECONDS = 60;

  private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");

  private final ReentrantLock refreshLock = new ReentrantLock();
  private volatile GoogleCredentials credentials;

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

  private static boolean isExpiringSoon(AccessToken token) {
    return token.getExpirationTime() == null || token.getExpirationTime().toInstant().isBefore(Instant.now().plusSeconds(REFRESH_LEAD_TIME_SECONDS));
  }
}
