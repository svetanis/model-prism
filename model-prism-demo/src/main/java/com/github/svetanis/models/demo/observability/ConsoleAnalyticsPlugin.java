package com.github.svetanis.models.demo.observability;

import com.google.adk.agents.InvocationContext;
import com.google.adk.events.Event;
import com.google.adk.plugins.BasePlugin;
import io.reactivex.rxjava3.core.Maybe;

/**
 * A simple custom ADK plugin that intercepts all telemetry events
 * and prints them to the standard console.
 */
public class ConsoleAnalyticsPlugin extends BasePlugin {

  public ConsoleAnalyticsPlugin() {
    super("console-analytics-plugin");
  }

  @Override
  public Maybe<Event> onEventCallback(InvocationContext ctx, Event event) {
    System.out.println("[ConsoleAnalyticsPlugin] Intercepted Event: " + event.getClass().getSimpleName());
    System.out.println("                         Details: " + event);
    return Maybe.empty();
  }
}
