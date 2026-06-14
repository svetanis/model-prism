package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing an available tool that the model can invoke.
 * Typically used to define callable functions via JSON schema.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiTool(
    @JsonProperty("type") String type,
    @JsonProperty("function") OpenAiFunction function
) {}

