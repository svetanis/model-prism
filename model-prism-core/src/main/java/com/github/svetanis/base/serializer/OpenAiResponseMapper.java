package com.github.svetanis.base.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.svetanis.base.serializer.dto.*;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Responsible for parsing raw JSON responses from OpenAI-compatible APIs and mapping them
 * back into ADK {@link LlmResponse} objects. Handles both full payloads and streamed deltas.
 */
class OpenAiResponseMapper {

  private final ObjectMapper mapper;

  OpenAiResponseMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  LlmResponse deserializeFullResponse(String responseBody) throws Exception {
    OpenAiChatResponse resp = mapper.readValue(responseBody, OpenAiChatResponse.class);
    if (resp.choices() != null && !resp.choices().isEmpty()) {
      OpenAiMessage message = resp.choices().get(0).message();
      if (message != null && message.toolCalls() != null && !message.toolCalls().isEmpty()) {
        return toolCallsResponse(message.toolCalls());
      }
      return finalTextResponse(message != null && message.content() != null ? message.content() : "");
    }
    return finalTextResponse("");
  }

  LlmResponse toolCallsResponse(List<OpenAiToolCall> toolCalls) throws JsonProcessingException {
    List<Part> parts = new ArrayList<>();
    for (OpenAiToolCall tc : toolCalls) {
      String argsJson = tc.function() != null && tc.function().arguments() != null ? tc.function().arguments() : "{}";
      String name = tc.function() != null && tc.function().name() != null ? tc.function().name() : "";

      @SuppressWarnings("unchecked")
      Map<String, Object> args = mapper.readValue(argsJson.isEmpty() ? "{}" : argsJson, Map.class);
      parts.add(Part.fromFunctionCall(name, args));
    }
    return LlmResponse.builder()
        .content(Content.builder().role("model").parts(parts).build())
        .turnComplete(false)
        .build();
  }

  LlmResponse partialTextResponse(String token) {
    return LlmResponse.builder()
        .content(Content.builder().role("model").parts(List.of(Part.fromText(token))).build())
        .partial(true)
        .turnComplete(false)
        .build();
  }

  LlmResponse finalTextResponse(String fullText) {
    return LlmResponse.builder()
        .content(Content.builder().role("model").parts(List.of(Part.fromText(fullText))).build())
        .partial(false)
        .turnComplete(true)
        .build();
  }
}
