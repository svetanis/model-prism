# model-prism-web

Spring Boot web application that runs the ADK Agent Runner and streams output using SSE (Server-Sent Events).

---

## Overview

This module demonstrates how to wrap an ADK `Runner` inside a Spring Boot REST API. It showcases how to:
- Configure the ADK `Runner` as a standard Spring `@Bean`.
- Filter the event stream (e.g., exposing only the output of a specific agent, like a synthesizer) while hiding intermediate tool or parallel agent outputs.
- Stream events to the browser using Spring Web's SSE capabilities.

## Architecture

- **`ModelPrismApp`**: Main Spring Boot application.
- **`ModelPrismAppConfig`**: Spring Configuration that sets up the `AgentLoader`, registering all `ModelProvider` SPI plugins dynamically. It also wires up our core services.
- **`ChatController` & `ChatSseController`**: Thin REST controllers handling HTTP request semantics and Server-Sent Event (SSE) connections.
- **`ChatService`**: The core business logic layer. It interacts with the ADK `Runner` and handles RxJava streams, decoupling AI execution from the HTTP controllers.
- **`RxSseEmitter`**: A utility adapter that safely bridges RxJava Observables into Spring `SseEmitter` streams.
- **`RootProvider`**: An overarching `LlmAgent` that orchestrates the workflow.
- **`ResearchPipelineProvider` & `ResearchPanelProvider`**: Demonstrates configuring a complex `SequentialAgent` pipeline provided as a tool to the root agent.

## Running the Web App

From the project root:

```bash
mvn -pl model-prism-web spring-boot:run
```

Or using `exec:java`:

```bash
mvn compile exec:java -pl model-prism-web -Dexec.mainClass="com.github.svetanis.models.web.app.ModelPrismApp"
```
