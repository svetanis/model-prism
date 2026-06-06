package com.github.svetanis.base.http.vertex;

import com.google.api.client.util.Preconditions;

public class EnvVarAccessTokenSupplier implements AccessTokenSupplier {

  private final String token;

  public EnvVarAccessTokenSupplier(String token) {
    this.token = Preconditions.checkNotNull(token, "token");
  }

  @Override
  public String getAccessToken() {
    return token;
  }
}
