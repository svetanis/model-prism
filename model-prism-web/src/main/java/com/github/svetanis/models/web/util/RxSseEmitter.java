package com.github.svetanis.models.web.util;

import io.reactivex.rxjava3.core.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Utility class for bridging RxJava {@link Observable} streams with Spring Web's
 * {@link SseEmitter}. It handles subscribing, sending data, and correctly
 * terminating the SSE connection on completion or error.
 */
public final class RxSseEmitter {

  private static final Logger log = LoggerFactory.getLogger(RxSseEmitter.class);

  private RxSseEmitter() {} // Utility class

  /**
   * Subscribes to the provided stream and sends each emitted item to the SseEmitter.
   * Completes the emitter if the stream finishes or errors out.
   *
   * @param stream  the RxJava stream of text data
   * @param emitter the Spring SseEmitter to push data to
   */
  public static void subscribeAndSend(Observable<String> stream, SseEmitter emitter) {
    stream.subscribe(
        data -> onNext(data, emitter),
        error -> onError(error, emitter),
        () -> onComplete(emitter)
    );
  }

  private static void onNext(String data, SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().data(data));
    } catch (Exception e) {
      log.error("Failed to send SSE event, client likely disconnected", e);
      emitter.completeWithError(e);
      throw new RuntimeException(e); // Cancel the RxJava subscription
    }
  }

  private static void onError(Throwable error, SseEmitter emitter) {
    log.error("Error in SSE stream", error);
    emitter.completeWithError(error);
  }

  private static void onComplete(SseEmitter emitter) {
    log.info("SSE stream completed successfully");
    emitter.complete();
  }
}
