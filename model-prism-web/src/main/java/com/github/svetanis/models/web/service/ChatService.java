package com.github.svetanis.models.web.service;

import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Observable;

/**
 * Service interface for handling ADK Runner chat interactions.
 * Decouples the underlying AI execution engine (e.g., RxJava observables)
 * from the HTTP presentation layer.
 */
public interface ChatService {
  
  /**
   * Executes a chat interaction and returns a stream of events.
   * Useful for Server-Sent Events (SSE) where partial responses are pushed to the client.
   *
   * @param request the chat request containing user ID, session ID, and topic
   * @return an {@link Observable} of {@link Event} containing agent responses
   */
  Observable<Event> runChatStream(ChatRequest request);
  
  /**
   * Executes a chat interaction synchronously, blocking until the entire response is complete.
   * Useful for standard REST API endpoints.
   *
   * @param request the chat request containing user ID, session ID, and topic
   * @return the fully aggregated text response from the root agent
   */
  String runChatSync(ChatRequest request);
}
