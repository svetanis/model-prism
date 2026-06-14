package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object representing a single message in an OpenAI conversation.
 * Represents system instructions, user prompts, assistant replies, or tool responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiMessage(
    @JsonProperty("role") String role,
    @JsonProperty("content") String content,
    @JsonProperty("tool_calls") List<OpenAiToolCall> toolCalls,
    @JsonProperty("tool_call_id") String toolCallId
) {}

