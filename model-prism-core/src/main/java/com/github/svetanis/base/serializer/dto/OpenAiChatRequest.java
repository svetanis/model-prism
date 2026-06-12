package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object representing the root JSON payload for an OpenAI Chat Completion request.
 * Contains the requested model, streaming preference, conversation history, and available tools.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiChatRequest(
    @JsonProperty("model") String model,
    @JsonProperty("stream") Boolean stream,
    @JsonProperty("messages") List<OpenAiMessage> messages,
    @JsonProperty("tools") List<OpenAiTool> tools
) {}
