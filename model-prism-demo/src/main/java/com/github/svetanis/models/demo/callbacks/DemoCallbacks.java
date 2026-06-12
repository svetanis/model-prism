package com.github.svetanis.models.demo.callbacks;

import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.InvocationContext;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.ToolContext;
import java.util.Map;
import java.util.Optional;

/**
 * Callback implementations for the {@link CallbacksDemoAgent}.
 *
 * <p>Contains static methods that serve as before/after hooks for both model
 * invocations and tool executions. The {@link #onBeforeTool} method doubles as
 * a guardrail, blocking specific ticker symbols.
 *
 * @see CallbacksDemoAgent
 */
public class DemoCallbacks {

  /**
   * Logs agent and user metadata before the model is invoked.
   *
   * @return {@link Optional#empty()} to proceed with the model call
   */
  protected static Optional<LlmResponse> onBeforeModel(
      CallbackContext ctx, LlmRequest.Builder requestBuilder) {
    String msg = " [CALLBACK] beforeModel -- agent=%s, userId=%s";
    System.out.println(msg.formatted(ctx.agentName(), ctx.userId()));
    return Optional.empty();
  }

  /**
   * Logs token usage statistics after a non-partial model response.
   *
   * @return {@link Optional#empty()} to use the original response
   */
  protected static Optional<LlmResponse> onAfterModel(CallbackContext ctx, LlmResponse response) {
    boolean isPartial = response.partial().orElse(false);
    boolean turnComplete = response.turnComplete().orElse(false);

    if (!isPartial) {
      String msg = " [tokens: prompt=%s, output=%s, total=%s]";
      String msg2 = " [CALLBACK] afterModel -- turnComplete=%s";
      String usage =
          response
              .usageMetadata()
              .map(
                  m ->
                      msg.formatted(
                          m.promptTokenCount().orElse(0),
                          m.candidatesTokenCount().orElse(0),
                          m.totalTokenCount().orElse(0)))
              .orElse("");
      System.out.println(msg2.formatted(turnComplete, usage));
    }
    return Optional.empty();
  }

  /**
   * Validates tool arguments before execution. Blocks the {@code "HACK"} ticker
   * symbol as a guardrail demonstration.
   *
   * @return a map with an error message if blocked, or {@link Optional#empty()} to proceed
   */
  protected static Optional<Map<String, Object>> onBeforeTool(
      InvocationContext ctx, BaseTool tool, Map<String, Object> args, ToolContext toolCtx) {
    String msg1 = " [CALLBACK] beforeTool -- tool=%s args=%s";
    System.out.println(msg1.formatted(tool.name(), args));
    Object ticker = args.get("ticker");
    if ("HACK".equalsIgnoreCase(String.valueOf(ticker))) {
      System.out.println(" [CALLBACK] beforeTool -- BLOCKED ticker: HACK (guardrail)");
      return Optional.of(Map.of("error", "Ticker 'HACK' is not permitted"));
    }
    return Optional.empty();
  }

  /**
   * Logs the tool name and result after execution.
   *
   * @return {@link Optional#empty()} to use the original result
   */
  protected static Optional<Map<String, Object>> onAfterTool(
      InvocationContext ctx,
      BaseTool tool,
      Map<String, Object> args,
      ToolContext toolCtx,
      Object result) {
    String msg = " [CALLBACK] afterTool -- tool=%s result=%s";
    System.out.println(msg.formatted(tool.name(), result));
    return Optional.empty();
  }
}
