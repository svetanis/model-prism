package com.github.svetanis.models.web.config;

import com.github.svetanis.models.web.agent.ResearchPipelineProvider;
import com.github.svetanis.models.spi.ModelProviderRegistry;
import com.google.adk.web.AdkWebServer;
import com.google.adk.web.AgentLoader;
import com.google.adk.web.AgentStaticLoader;
import com.github.svetanis.models.web.agent.RootProvider;
import com.github.svetanis.models.web.service.ChatService;
import com.github.svetanis.models.web.service.DefaultChatService;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.AgentTool;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.runner.Runner;
import com.google.adk.runner.InMemoryRunner;

import jakarta.annotation.PostConstruct;

@Configuration
@Import(AdkWebServer.class)
public class ModelPrismAppConfig {

  private static final Logger logger = Logger.getLogger(ModelPrismAppConfig.class.getName());

  @Value("${adk.agent.model:openrouter/auto}")
  public String model;

  @Value("${google.api.key:}")
  public String googleApiKey;

  @PostConstruct
  public void init() {
    if (StringUtils.isNotBlank(googleApiKey)) {
      System.setProperty("GOOGLE_API_KEY", googleApiKey);
      // Also set GEMINI_API_KEY just in case the provider looks for that specifically
      System.setProperty("GEMINI_API_KEY", googleApiKey);
    }
  }

  @Bean
  LlmAgent rootAgent() {
    return new RootProvider(model, researchPipeline()).get();
  }

  @Bean
  AgentLoader agentLoader() {
    logger.info("Registering model providers...");
    ModelProviderRegistry.registerAll();
    return new AgentStaticLoader(rootAgent(), researchPipeline());
  }

  @Bean
  SequentialAgent researchPipeline() {
    return new ResearchPipelineProvider(model).get();
  }

  @Bean 
  Runner runner() { 
    ModelProviderRegistry.registerAll(); 
    return new InMemoryRunner(rootAgent(), "adk-web-app"); 
  }

  @Bean
  ChatService chatService(Runner runner) {
    return new DefaultChatService(runner);
  }
}

