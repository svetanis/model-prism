package com.github.svetanis.models.web.controller;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.github.svetanis.models.web.service.ChatRequest;
import com.github.svetanis.models.web.service.ChatService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standard REST controller for synchronous chat interactions.
 * <p>
 * This controller blocks until the entire LLM response is generated and returns
 * the final text response in a single HTTP payload.
 */
@RestController
@RequestMapping("/api")
public class ChatController {

  private static final Logger log = LoggerFactory.getLogger(ChatController.class);

  private final ChatService chatService;

  @Inject
  public ChatController(ChatService chatService) {
    this.chatService = checkNotNull(chatService);
  }

  @PostMapping(value = "/chat", consumes = APPLICATION_JSON_VALUE, produces = TEXT_PLAIN_VALUE)
  public ResponseEntity<?> chatSync(@RequestBody ChatRequest request) {
    try {
      log.info("Received chat request: {}", request);
      String responseText = chatService.runChatSync(request);
      return ResponseEntity.ok(responseText);
    } catch (Throwable e) {
      log.error("Error processing chat request", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}

