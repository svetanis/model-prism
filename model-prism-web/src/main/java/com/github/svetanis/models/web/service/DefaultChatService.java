package com.github.svetanis.models.web.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.adk.agents.RunConfig;
import com.google.adk.runner.Runner;
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ChatService}.
 * <p>
 * This class encapsulates the ADK {@link Runner} orchestration logic. It converts
 * a {@link ChatRequest} into a {@link RunConfig} and filters the returned event
 * stream for responses originating from the root agent.
 */
public class DefaultChatService implements ChatService {

  private final Runner runner;

  @Inject
  public DefaultChatService(Runner runner) {
    this.runner = checkNotNull(runner);
  }

  @Override
  public Observable<Event> runChatStream(ChatRequest request) {
    Content userMessage = Content.fromParts(Part.fromText(request.topic()));
    RunConfig config = RunConfig.builder().autoCreateSession(true).build();
    
    return runner.runAsync(request.userId(), request.sessionId(), userMessage, config)
        .filter(event -> "root".equals(event.author()))
        .toObservable();
  }

  @Override
  public String runChatSync(ChatRequest request) {
    return runChatStream(request)
        .map(event -> event.stringifyContent())
        .filter(text -> isNotBlank(text))
        .toList()
        .blockingGet()
        .stream()
        .collect(Collectors.joining());
  }
}
