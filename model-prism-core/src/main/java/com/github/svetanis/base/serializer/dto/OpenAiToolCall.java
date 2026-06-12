package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing a specific tool invocation requested by the model.
 * Often used to wrap an {@link OpenAiFunctionCall}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiToolCall(
    @JsonProperty("id") String id,
    @JsonProperty("type") String type,
    @JsonProperty("function") OpenAiFunctionCall function,
    @JsonProperty("index") Integer index
) {}
