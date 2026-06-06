package com.github.svetanis.base.http.vertex;

import java.io.IOException;

public interface AccessTokenSupplier {

  String getAccessToken() throws IOException;
  
  static AccessTokenSupplier autoDetect() {
    String envToken = System.getenv("VERTEX_ACCESS_TOKEN");
    if(envToken != null && !envToken.isBlank()) {
      return new EnvVarAccessTokenSupplier(envToken);
    }
    return new AdcAccessTokenSupplier();
  }
  
}
