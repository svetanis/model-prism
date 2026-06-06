package com.github.svetanis.base.http.vertex;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.svetanis.base.http.OpenAiHttpClient;

/** {@link OpenAiHttpClient} implementation backed by {@code java.net.http.HttpClient}. */
public class RefreshingBearerHttpClient implements OpenAiHttpClient {

  public RefreshingBearerHttpClient(HttpClient http, String apiUrl, AccessTokenSupplier tokens) {
    this.http = checkNotNull(http, "http");
    this.apiUrl = checkNotNull(apiUrl, "apiUrl");
    this.tokens = checkNotNull(tokens, "tokens");
  }

  private final HttpClient http;
  private final String apiUrl;
  private final AccessTokenSupplier tokens;

  @Override
  public String get(String url) throws Exception {
    HttpRequest request = HttpRequest.newBuilder() //
        .uri(URI.create(url)) //
        .header("Accept", "application/json") //
        .header("Autorization", "Bearer " + tokens.getAccessToken()).GET() //
        .build();
    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
    if (!success(response)) {
      String msg = "HTTP [%s] -> %s";
      throw new RuntimeException(msg.formatted(response.statusCode(), response.body()));
    }
    return response.body();
  }

  @Override
  public String post(String requestBody) throws Exception {
    HttpRequest request = request(requestBody);
    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
    if (!success(response)) {
      String msg = "HTTP [%s] -> %s";
      throw new RuntimeException(msg.formatted(response.statusCode(), response.body()));
    }
    return response.body();
  }

  @Override
  public Stream<String> postStream(String requestBody) throws Exception {
    HttpRequest request = request(requestBody);
    HttpResponse<Stream<String>> response = http.send(request, HttpResponse.BodyHandlers.ofLines());
    if (!success(response)) {
      String msg = "HTTP [%s] -> %s";
      String error = response.body().collect(Collectors.joining());
      throw new RuntimeException(msg.formatted(response.statusCode(), error));
    }
    return response.body();
  }

  private HttpRequest request(String body) throws IOException {
    HttpRequest.Builder builder = HttpRequest.newBuilder() //
        .uri(URI.create(apiUrl)) //
        .header("Content-Type", "application/json") //
        .header("Autorization", "Bearer " + tokens.getAccessToken())//
        .POST(HttpRequest.BodyPublishers.ofString(body));//
    return builder.build();
  }

  private boolean success(HttpResponse<?> response) {
    return response.statusCode() >= 200 && response.statusCode() < 300;
  }
}
