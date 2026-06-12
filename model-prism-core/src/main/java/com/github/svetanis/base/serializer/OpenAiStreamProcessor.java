package com.github.svetanis.base.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.svetanis.base.serializer.dto.*;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.FlowableEmitter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Handles the processing of Server-Sent Events (SSE) from streaming OpenAI-compatible APIs.
 * Accumulates chunked text and fragmented tool call arguments, emitting them to the observer.
 */
class OpenAiStreamProcessor {

  private static final String SSE_DATA_PREFIX = "data: ";
  private static final String SSE_DONE = "data: [DONE]";

  private final ObjectMapper mapper;
  private final OpenAiResponseMapper responseMapper;

  OpenAiStreamProcessor(ObjectMapper mapper, OpenAiResponseMapper responseMapper) {
    this.mapper = mapper;
    this.responseMapper = responseMapper;
  }

  void processStreamLines(Stream<String> lines, FlowableEmitter<LlmResponse> emitter) throws Exception {
    StringBuilder sb = new StringBuilder();
    List<OpenAiToolCall> nodes = new ArrayList<>();
    Iterator<String> it = lines.iterator();

    while (it.hasNext()) {
      String line = it.next();
      if (SSE_DONE.equals(line)) break;
      if (!line.startsWith(SSE_DATA_PREFIX)) continue;

      String json = line.substring(SSE_DATA_PREFIX.length()).trim();
      if (isBlank(json)) continue;

      OpenAiChatResponse chunk = parseChunk(json);
      if (chunk == null) continue;

      processDelta(chunk, sb, nodes, emitter);
    }

    emitter.onNext(nodes.isEmpty() ? responseMapper.finalTextResponse(sb.toString()) : responseMapper.toolCallsResponse(nodes));
  }

  private OpenAiChatResponse parseChunk(String json) {
    try {
      OpenAiChatResponse chunk = mapper.readValue(json, OpenAiChatResponse.class);
      if (chunk.error() != null) {
        throw new IllegalArgumentException("Stream error: " + chunk.error().message());
      }
      return chunk;
    } catch (JsonProcessingException e) {
      return null;
    }
  }

  private void processDelta(OpenAiChatResponse chunk, StringBuilder sb, List<OpenAiToolCall> nodes, FlowableEmitter<LlmResponse> emitter) {
    if (chunk.choices() == null || chunk.choices().isEmpty()) return;

    OpenAiMessage delta = chunk.choices().get(0).delta();
    if (delta == null) return;

    String textDelta = delta.content();
    if (isNotEmpty(textDelta)) {
      sb.append(textDelta);
      emitter.onNext(responseMapper.partialTextResponse(textDelta));
    }

    accumulateToolCalls(delta.toolCalls(), nodes);
  }

  private void accumulateToolCalls(List<OpenAiToolCall> toolCalls, List<OpenAiToolCall> nodes) {
    if (toolCalls == null) return;

    for (OpenAiToolCall tc : toolCalls) {
      int index = tc.index() != null ? tc.index() : 0;
      while (nodes.size() <= index) {
        nodes.add(new OpenAiToolCall(null, null, new OpenAiFunctionCall("", ""), index));
      }
      
      OpenAiToolCall acc = nodes.get(index);
      String newId = tc.id() != null ? tc.id() : acc.id();
      String newName = acc.function().name();
      String newArgs = acc.function().arguments();

      if (tc.function() != null) {
        if (tc.function().name() != null) newName = tc.function().name();
        if (tc.function().arguments() != null) newArgs += tc.function().arguments();
      }

      nodes.set(index, new OpenAiToolCall(newId, acc.type(), new OpenAiFunctionCall(newName, newArgs), index));
    }
  }
}
