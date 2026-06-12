package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Data Transfer Object representing the definition of an callable function.
 * Includes the function name, description, and its expected JSON schema parameters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiFunction(
    @JsonProperty("name") String name,
    @JsonProperty("description") String description,
    @JsonProperty("parameters") JsonNode parameters
) {}
