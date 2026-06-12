package com.github.svetanis.models.web.service;

/**
 * Data Transfer Object (DTO) for representing an incoming chat request.
 *
 * @param userId    the unique identifier of the user making the request
 * @param sessionId the conversation session identifier
 * @param topic     the text or topic provided by the user
 */
public record ChatRequest(String userId, String sessionId, String topic) {}
