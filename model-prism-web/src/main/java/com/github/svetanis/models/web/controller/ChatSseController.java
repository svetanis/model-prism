package com.github.svetanis.models.web.controller;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.github.svetanis.models.web.service.ChatRequest;
import com.github.svetanis.models.web.service.ChatService;
import com.github.svetanis.models.web.util.RxSseEmitter;
import io.reactivex.rxjava3.core.Observable;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for handling streaming chat interactions via Server-Sent Events (SSE).
 * <p>
 * This controller pushes partial text chunks to the client as they are generated
 * by the LLM, providing a real-time typing effect.
 */
@RestController
public class ChatSseController {

  private static final Logger log = LoggerFactory.getLogger(ChatSseController.class);

  private final ChatService chatService;

  @Inject
  public ChatSseController(ChatService chatService) {
    this.chatService = checkNotNull(chatService);
  }

  @GetMapping(value = "/chat-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter chat(
      @RequestParam String userId, @RequestParam String sessionId, @RequestParam String topic) {
    log.info("Received SSE chat request for topic: {}", topic);
    SseEmitter emitter = new SseEmitter(120_000L); // 2 min timeout
    
    ChatRequest request = new ChatRequest(userId, sessionId, topic);
    Observable<String> textStream = chatService.runChatStream(request)
        .map(event -> (String) event.stringifyContent())
        .filter(text -> isNotBlank(text));
        
    RxSseEmitter.subscribeAndSend(textStream, emitter);
    
    return emitter;
  }
}

