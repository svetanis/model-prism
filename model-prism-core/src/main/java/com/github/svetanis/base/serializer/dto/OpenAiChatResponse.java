package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object representing the root JSON payload for an OpenAI Chat Completion response.
 * Contains the list of generated choices and any potential API errors.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiChatResponse(
    @JsonProperty("choices") List<OpenAiChoice> choices,
    @JsonProperty("error") OpenAiError error
) {}
