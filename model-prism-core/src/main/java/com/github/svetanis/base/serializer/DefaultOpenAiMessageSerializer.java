package com.github.svetanis.base.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import io.reactivex.rxjava3.core.FlowableEmitter;
import java.util.stream.Stream;

/** Jackson-backed {@link OpenAiMessageSerializer} for the OpenAI chat-completions format. */
public final class DefaultOpenAiMessageSerializer implements OpenAiMessageSerializer {

  private final OpenAiRequestMapper requestMapper;
  private final OpenAiResponseMapper responseMapper;
  private final OpenAiStreamProcessor streamProcessor;

  public DefaultOpenAiMessageSerializer() {
    ObjectMapper mapper = new ObjectMapper();
    this.requestMapper = new OpenAiRequestMapper(mapper);
    this.responseMapper = new OpenAiResponseMapper(mapper);
    this.streamProcessor = new OpenAiStreamProcessor(mapper, responseMapper);
  }

  @Override
  public String serializeRequest(LlmRequest request, String modelName, boolean stream) throws Exception {
    return requestMapper.serialize(request, modelName, stream);
  }

  @Override
  public LlmResponse deserializeResponse(String responseBody) throws Exception {
    return responseMapper.deserializeFullResponse(responseBody);
  }

  @Override
  public void processStreamLines(Stream<String> lines, FlowableEmitter<LlmResponse> emitter) throws Exception {
    streamProcessor.processStreamLines(lines, emitter);
  }
}
