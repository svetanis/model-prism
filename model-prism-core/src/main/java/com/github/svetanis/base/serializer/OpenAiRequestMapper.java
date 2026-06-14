package com.github.svetanis.base.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.svetanis.base.serializer.dto.*;
import com.google.adk.models.LlmRequest;
import com.google.adk.tools.BaseTool;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Responsible for mapping an ADK {@link LlmRequest} into the OpenAI-compatible JSON representation.
 * It translates system instructions, text prompts, and tool definitions into the corresponding DTOs.
 */
class OpenAiRequestMapper {

  private final ObjectMapper mapper;

  OpenAiRequestMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  String serialize(LlmRequest request, String modelName, boolean stream) throws JsonProcessingException {
    List<OpenAiMessage> messages = new ArrayList<>();
    addSystemInstruction(request, messages);
    addContents(request, messages);
    List<OpenAiTool> tools = getTools(request);

    OpenAiChatRequest req = new OpenAiChatRequest(modelName, stream, messages, tools);
    return mapper.writeValueAsString(req);
  }

  private void addSystemInstruction(LlmRequest request, List<OpenAiMessage> messages) {
    request.getFirstSystemInstruction().ifPresent(inst -> 
        messages.add(new OpenAiMessage("system", inst, null, null)));
  }

  private void addContents(LlmRequest request, List<OpenAiMessage> messages) throws JsonProcessingException {
    for (Content content : request.contents()) {
      String role = resolveRole(content);
      List<Part> parts = content.parts().orElse(List.of());

      if (tryAddFunctionCalls(parts, messages)) continue;
      if (tryAddFunctionResponses(parts, messages)) continue;

      String text = content.text();
      if (isNotBlank(text)) {
        messages.add(new OpenAiMessage(role, text, null, null));
      }
    }
  }

  private String resolveRole(Content content) {
    String role = content.role().orElse("user");
    return "model".equals(role) ? "assistant" : role;
  }

  private boolean tryAddFunctionCalls(List<Part> parts, List<OpenAiMessage> messages) throws JsonProcessingException {
    List<Part> callParts = parts.stream().filter(p -> p.functionCall().isPresent()).toList();
    if (callParts.isEmpty()) return false;

    List<OpenAiToolCall> toolCalls = new ArrayList<>();
    for (Part part : callParts) {
      FunctionCall fc = part.functionCall().get();
      String argsJson = mapper.writeValueAsString(fc.args().orElse(Map.of()));
      toolCalls.add(new OpenAiToolCall(
          fc.id().orElse(fc.name().orElse("call_0")),
          "function",
          new OpenAiFunctionCall(fc.name().orElse(""), argsJson),
          null
      ));
    }
    messages.add(new OpenAiMessage("assistant", null, toolCalls, null));
    return true;
  }

  private boolean tryAddFunctionResponses(List<Part> parts, List<OpenAiMessage> messages) throws JsonProcessingException {
    List<Part> respParts = parts.stream().filter(p -> p.functionResponse().isPresent()).toList();
    if (respParts.isEmpty()) return false;

    for (Part part : respParts) {
      var fr = part.functionResponse().get();
      String respJson = mapper.writeValueAsString(fr.response().orElse(Map.of()));
      messages.add(new OpenAiMessage(
          "tool", respJson, null, fr.id().orElse(fr.name().orElse("call_0"))
      ));
    }
    return true;
  }

  private List<OpenAiTool> getTools(LlmRequest request) throws JsonProcessingException {
    if (request.tools().isEmpty()) return null;

    List<OpenAiTool> tools = new ArrayList<>();
    for (BaseTool tool : request.tools().values()) {
      Optional<FunctionDeclaration> declOpt = tool.declaration();
      if (declOpt.isPresent()) {
        FunctionDeclaration decl = declOpt.get();
        JsonNode params = decl.parameters().isPresent() 
            ? normalizeSchemaTypes(mapper.readTree(decl.parameters().get().toJson()))
            : mapper.createObjectNode().put("type", "object").set("properties", mapper.createObjectNode());

        tools.add(new OpenAiTool("function", new OpenAiFunction(
            decl.name().orElse(""), decl.description().orElse(""), params
        )));
      }
    }
    return tools;
  }

  private JsonNode normalizeSchemaTypes(JsonNode node) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      if (obj.has("type")) {
        obj.put("type", obj.get("type").asText().toLowerCase());
        if ("object".equals(obj.get("type").asText()) && !obj.has("properties")) {
          obj.set("properties", mapper.createObjectNode());
        }
      }
      // Generically traverse ALL fields to find nested schemas (e.g. inside "items" or "properties")
      obj.properties().forEach(entry -> normalizeSchemaTypes(entry.getValue()));
    } else if (node.isArray()) {
      node.forEach(child -> normalizeSchemaTypes(child));
    }
    return node;
  }
}
