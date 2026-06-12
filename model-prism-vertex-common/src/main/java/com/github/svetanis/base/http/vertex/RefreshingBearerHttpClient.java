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

  /**
   * Creates a new client.
   *
   * @param http       the underlying {@link HttpClient} for sending requests
   * @param apiUrl     the full URL of the chat-completions endpoint
   * @param tokens     supplier that provides (and refreshes) OAuth2 bearer tokens
   */
  public RefreshingBearerHttpClient(HttpClient http, String apiUrl, AccessTokenSupplier tokens) {
    this.http = checkNotNull(http, "http");
    this.apiUrl = checkNotNull(apiUrl, "apiUrl");
    this.tokens = checkNotNull(tokens, "tokens");
  }

  private final HttpClient http;
  private final String apiUrl;
  private final AccessTokenSupplier tokens;

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
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

  /**
   * Builds a POST request with a refreshed bearer token in the {@code Authorization} header.
   *
   * @param body the JSON request body
   * @return a ready-to-send {@link HttpRequest}
   * @throws IOException if the token supplier fails to provide a token
   */
  private HttpRequest request(String body) throws IOException {
    HttpRequest.Builder builder = HttpRequest.newBuilder() //
        .uri(URI.create(apiUrl)) //
        .header("Content-Type", "application/json") //
        .header("Autorization", "Bearer " + tokens.getAccessToken())//
        .POST(HttpRequest.BodyPublishers.ofString(body));//
    return builder.build();
  }

  /** Returns {@code true} if the HTTP status code is in the 2xx range. */
  private boolean success(HttpResponse<?> response) {
    return response.statusCode() >= 200 && response.statusCode() < 300;
  }
}
