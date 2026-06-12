package com.github.svetanis.models.demo.callbacks;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.adk.agents.Callbacks;
import com.google.adk.agents.LlmAgent;
import com.github.svetanis.models.demo.tools.DemoTools;
import com.google.adk.tools.FunctionTool;
import jakarta.inject.Provider;

/**
 * {@link Provider} that builds a stock-price assistant {@link LlmAgent} wired with
 * all four callback hooks: {@code beforeModel}, {@code afterModel}, {@code beforeTool},
 * and {@code afterTool}.
 *
 * <p>The {@code beforeTool} callback acts as a guardrail, blocking requests for
 * the ticker symbol {@code "HACK"} to demonstrate input validation.
 *
 * @see DemoCallbacks
 * @see CallbacksDemoApp
 */
public final class CallbacksDemoAgent implements Provider<LlmAgent> {

  private static final String INSTRUCTION =
      """
      You are a stock-price assistant.
      Use the lookupStockPrice tool
      to answer questions.
      """;

  public CallbacksDemoAgent(String model) {
    this.model = checkNotNull(model, "model");
  }

  private final String model;

  @Override
  public LlmAgent get() {
    Callbacks.BeforeModelCallbackSync beforeModel = DemoCallbacks::onBeforeModel;
    Callbacks.AfterModelCallbackSync afterModel = DemoCallbacks::onAfterModel;
    Callbacks.BeforeToolCallbackSync beforeTool = DemoCallbacks::onBeforeTool;
    Callbacks.AfterToolCallbackSync afterTool = DemoCallbacks::onAfterTool;

    return LlmAgent.builder()
        .name("callback-demo-agent") //
        .model(model) //
        .description("Stock Price Assistant") //
        .instruction(INSTRUCTION) //
        .tools(FunctionTool.create(DemoTools.class, "lookupStockPrice"))
        .beforeModelCallbackSync(beforeModel)
        .afterModelCallbackSync(afterModel)
        .beforeToolCallbackSync(beforeTool)
        .afterToolCallbackSync(afterTool)
        .build();
  }
}
