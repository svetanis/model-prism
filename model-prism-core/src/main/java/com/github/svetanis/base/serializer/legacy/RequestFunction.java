package com.github.svetanis.base.serializer.legacy;

import static com.github.svetanis.base.serializer.legacy.SerializerUtils.ARGUMENTS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.ASSISTANT;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.CALL_0;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.CONTENT;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.DESCRIPTION;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.FUNCTION;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.ID;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.MAPPER;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.MESSAGES;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.MODEL;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.NAME;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.OBJECT;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.PARAMETERS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.ROLE;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.STREAM;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.SYSTEM;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TOOL;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TOOLS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TOOL_CALLS;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TOOL_CALL_ID;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.TYPE;
import static com.github.svetanis.base.serializer.legacy.SerializerUtils.USER;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.adk.models.LlmRequest;
import com.google.adk.tools.BaseTool;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionDeclaration;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Converts an ADK {@link LlmRequest} into an OpenAI chat-completions JSON request body.
 *
 * <p>Implements {@link Function}{@code <LlmRequest, String>} so it can be used inline
 * with functional pipelines. Handles system instructions, user/assistant/tool messages,
 * function call parts, function response parts, and tool declarations.
 *
 * @see ResponseFunction
 * @see DefaultOpenAiMessageSerializer
 */
@Deprecated public final class RequestFunction implements Function<LlmRequest, String> {

  /**
   * Creates a new request serializer.
   *
   * @param modelName the bare model identifier (without provider prefix) to embed
   *                  in the JSON {@code model} field
   * @param stream    {@code true} to include {@code "stream": true} in the payload
   */
  public RequestFunction(String modelName, boolean stream) {
    this.stream = stream;
    this.modelName = checkNotNull(modelName, "modelName");
  }

  private final boolean stream;
  private final String modelName;

  /**
   * Serializes the given {@link LlmRequest} into an OpenAI-format JSON string.
   *
   * @param request the ADK request containing messages, tools, and system instructions
   * @return a JSON string suitable for posting to a chat-completions endpoint
   * @throws IllegalArgumentException if serialization fails
   */
  @Override
  public String apply(LlmRequest request) {
    try {
      ObjectNode body = MAPPER.createObjectNode();
      body.put(MODEL, modelName);
      body.put(STREAM, stream);

      ArrayNode messages = body.putArray(MESSAGES);
      request
          .getFirstSystemInstruction()
          .ifPresent(inst -> messages.addObject().put(ROLE, SYSTEM).put(CONTENT, inst));
      for (Content content : request.contents()) {
        String role = content.role().orElse(USER);
        if (MODEL.equals(role)) {
          role = ASSISTANT;
        }
        appendContentParts(messages, role, content);
      }

      if (!request.tools().isEmpty()) {
        ArrayNode tools = body.putArray(TOOLS);
        for (BaseTool tool : request.tools().values()) {
          tool.declaration().ifPresent(decl -> appendToolDeclaration(tools, decl));
        }
      }
      return MAPPER.writeValueAsString(body);
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Appends the parts of a {@link Content} message to the JSON messages array.
   *
   * <p>Dispatches to specialised handlers based on the part type:
   * function-call parts, function-response parts, or plain text.
   */
  private void appendContentParts(ArrayNode messages, String role, Content content)
      throws Exception {
    List<Part> parts = content.parts().orElse(List.of());
    List<Part> callParts = parts.stream().filter(p -> p.functionCall().isPresent()).toList();
    if (!callParts.isEmpty()) {
      appendCallParts(messages, callParts);
      return;
    }
    List<Part> respParts = parts.stream().filter(p -> p.functionResponse().isPresent()).toList();
    if (!respParts.isEmpty()) {
      appendResponseParts(messages, respParts);
      return;
    }
    String text = content.text();
    if (isNotBlank(text)) {
      messages.addObject().put(ROLE, role).put(CONTENT, text);
    }
  }

  /**
   * Serializes function-response parts as OpenAI {@code tool}-role messages.
   */
  private void appendResponseParts(ArrayNode messages, List<Part> responseParts) throws Exception {
    for (Part part : responseParts) {
      var fr = part.functionResponse().get();
      messages
          .addObject() //
          .put(ROLE, TOOL) //
          .put(TOOL_CALL_ID, fr.id().orElse(fr.name().orElse(CALL_0))) //
          .put(CONTENT, MAPPER.writeValueAsString(fr.response().orElse(Map.of())));
    }
  }

  /**
   * Serializes function-call parts as an {@code assistant} message with
   * a {@code tool_calls} array.
   */
  private void appendCallParts(ArrayNode messages, List<Part> callParts) throws Exception {
    ObjectNode msg = messages.addObject();
    msg.put(ROLE, ASSISTANT);
    ArrayNode toolCalls = msg.putArray(TOOL_CALLS);
    for (Part part : callParts) {
      FunctionCall fc = part.functionCall().get();
      ObjectNode tc = toolCalls.addObject();
      tc.put(ID, fc.id().orElse(fc.name().orElse(CALL_0)));
      tc.put(TYPE, FUNCTION);
      tc.putObject(FUNCTION) //
          .put(NAME, fc.name().orElse("")) //
          .put(ARGUMENTS, MAPPER.writeValueAsString(fc.args().orElse(Map.of())));
    }
  }

  /**
   * Converts a {@link FunctionDeclaration} into an OpenAI-format tool declaration
   * and appends it to the tools array.
   */
  private void appendToolDeclaration(ArrayNode toolsArray, FunctionDeclaration decl) {
    try {
      ObjectNode node = toolsArray.addObject().put(TYPE, FUNCTION).putObject(FUNCTION);
      node.put(NAME, decl.name().orElse(""));
      node.put(DESCRIPTION, decl.description().orElse(""));
      if (decl.parameters().isPresent()) {
        node.set(
            PARAMETERS, normalizeSchemaTypes(MAPPER.readTree(decl.parameters().get().toJson())));
      } else {
        node.putObject(PARAMETERS).put(TYPE, OBJECT);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to serialize tool declaration: " + decl.name(), e);
    }
  }

  /**
   * Recursively normalises JSON Schema {@code type} fields to lowercase.
   *
   * <p>Some SDKs emit {@code "STRING"} or {@code "Integer"} — the OpenAI API
   * expects lowercase ({@code "string"}, {@code "integer"}).
   */
  private JsonNode normalizeSchemaTypes(JsonNode node) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      if (obj.has(TYPE)) {
        obj.put(TYPE, obj.get(TYPE).asText().toLowerCase());
      }
      obj.fields().forEachRemaining(entry -> normalizeSchemaTypes(entry.getValue()));
    } else if (node.isArray()) {
      node.forEach(child -> normalizeSchemaTypes(child));
    }
    return node;
  }
}
