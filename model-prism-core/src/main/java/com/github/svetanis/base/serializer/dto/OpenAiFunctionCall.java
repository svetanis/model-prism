package com.github.svetanis.base.serializer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object representing the execution details of a requested function.
 * Contains the name of the function and the JSON arguments to pass to it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiFunctionCall(
    @JsonProperty("name") String name,
    @JsonProperty("arguments") String arguments
) {}
