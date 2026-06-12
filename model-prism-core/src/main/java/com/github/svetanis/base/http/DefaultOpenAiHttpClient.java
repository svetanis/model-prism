package com.github.svetanis.base.http;

import static com.google.api.client.util.Preconditions.checkNotNull;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** {@link OpenAiHttpClient} implementation backed by {@code java.net.http.HttpClient}. */
public class DefaultOpenAiHttpClient implements OpenAiHttpClient {

  private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newHttpClient();

  private final HttpClient http;
  private final String apiUrl;
  private final Optional<String> apiKey;

  /**
   * Creates a new client targeting the given API URL using a shared {@link HttpClient}.
   *
   * @param apiUrl the full URL of the chat-completions endpoint
   * @param apiKey optional API key for the {@code Authorization: Bearer} header;
   *               if empty, no auth header is sent (suitable for Ollama and other
   *               key-less APIs)
   */
  public DefaultOpenAiHttpClient(String apiUrl, Optional<String> apiKey) {
    this(SHARED_HTTP_CLIENT, apiUrl, apiKey);
  }

  /**
   * Creates a new client targeting the given API URL with a custom {@link HttpClient}.
   */
  public DefaultOpenAiHttpClient(HttpClient http, String apiUrl, Optional<String> apiKey) {
    this.http = checkNotNull(http, "http");
    this.apiUrl = checkNotNull(apiUrl, "apiUrl");
    this.apiKey = checkNotNull(apiKey, "apiKey");
  }

  /** {@inheritDoc} */
  @Override
  public String get(String url) throws Exception {
    HttpRequest request = newRequestBuilder(url)
        .header("Accept", "application/json")
        .GET()
        .build();
    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
    return getBodyOrThrow(response);
  }

  /** {@inheritDoc} */
  @Override
  public String post(String requestBody) throws Exception {
    HttpRequest request = newRequestBuilder(apiUrl)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();
    HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
    return getBodyOrThrow(response);
  }

  /** {@inheritDoc} */
  @Override
  public Stream<String> postStream(String requestBody) throws Exception {
    HttpRequest request = newRequestBuilder(apiUrl)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();
    HttpResponse<Stream<String>> response = http.send(request, HttpResponse.BodyHandlers.ofLines());
    if (!success(response)) {
      String error = response.body().collect(Collectors.joining());
      throw new RuntimeException("HTTP [%s] -> %s".formatted(response.statusCode(), error));
    }
    return response.body();
  }

  /**
   * Creates a base request builder with the URI and the Authorization header if an API key is present.
   */
  private HttpRequest.Builder newRequestBuilder(String url) {
    HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url));
    apiKey.ifPresent(key -> builder.header("Authorization", "Bearer " + key));
    return builder;
  }

  private String getBodyOrThrow(HttpResponse<String> response) {
    if (!success(response)) {
      throw new RuntimeException("HTTP [%s] -> %s".formatted(response.statusCode(), response.body()));
    }
    return response.body();
  }

  /** Returns {@code true} if the HTTP status code is in the 2xx range. */
  private boolean success(HttpResponse<?> response) {
    return response.statusCode() >= 200 && response.statusCode() < 300;
  }
}
