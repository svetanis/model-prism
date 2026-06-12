package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing an error returned by the OpenAI API.
 * Contains the error message and the type of error.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiError(
    @JsonProperty("message") String message,
    @JsonProperty("type") String type
) {}
