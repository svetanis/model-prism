package com.github.svetanis.base.serializer.legacy;

import static com.github.svetanis.base.serializer.legacy.SerializerUtils.ARGUMENTS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.CHOICES;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.CONTENT;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.FUNCTION;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.MAPPER;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.MESSAGE;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.NAME;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TOOL_CALLS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.content;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.finalTextResponse;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.parseJsonArgs;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Part;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Deserializes an OpenAI chat-completions JSON response body into an ADK {@link LlmResponse}.
 *
 * <p>Implemented as a singleton enum to provide a thread-safe, lazily-initialised instance.
 * Handles both plain-text responses and responses containing tool calls.
 *
 * @see RequestFunction
 * @see DefaultOpenAiMessageSerializer
 */
@Deprecated public enum ResponseFunction implements Function<String, LlmResponse> {
  /** The singleton deserializer instance. */
  INSTANCE;

  /**
   * Parses the given JSON response body and returns an {@link LlmResponse}.
   *
   * <p>If the response contains a {@code tool_calls} array, returns a tool-call
   * response with {@code turnComplete = false}. Otherwise, returns a final text
   * response with {@code turnComplete = true}.
   *
   * @param input the raw JSON response body from the chat-completions API
   * @return the deserialized {@link LlmResponse}
   * @throws IllegalArgumentException if the JSON cannot be parsed
   */
  @Override
  public LlmResponse apply(String input) {
    try {
      JsonNode root = MAPPER.readTree(input);
      JsonNode message = root.path(CHOICES).path(0).path(MESSAGE);
      JsonNode toolCalls = message.path(TOOL_CALLS);
      if (toolCalls.isArray() && !toolCalls.isEmpty()) {
        return toolCallsResponse(toolCalls);
      }
      return finalTextResponse(message.path(CONTENT).asText(""));
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Builds an {@link LlmResponse} containing one or more function-call parts
   * extracted from the {@code tool_calls} JSON array.
   */
  private LlmResponse toolCallsResponse(JsonNode toolCalls) throws Exception {
    List<Part> parts = new ArrayList<>();
    for (JsonNode tc : toolCalls) {
      String txt = tc.path(FUNCTION).path(ARGUMENTS).asText("{}");
      String name = tc.path(FUNCTION).path(NAME).asText("");
      Map<String, Object> args = parseJsonArgs(txt);
      parts.add(Part.fromFunctionCall(name, args));
    }
    return LlmResponse.builder() //
        .content(content(parts)) //
        .turnComplete(false)
        .build(); //
  }
}
