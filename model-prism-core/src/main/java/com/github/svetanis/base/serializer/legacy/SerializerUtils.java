package com.github.svetanis.base.serializer.legacy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.models.LlmResponse;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Map;

/**
 * Shared JSON field-name constants, an {@link ObjectMapper} singleton, and small
 * utility methods used by {@link RequestFunction}, {@link ResponseFunction}, and
 * {@link DefaultOpenAiMessageSerializer}.
 *
 * <p>This class is package-private by convention — all members are {@code protected}
 * to restrict visibility to the {@code serializer} package.
 */
@Deprecated public final class SerializerUtils {

  /** Reusable Jackson {@link ObjectMapper} — thread-safe for reading and writing. */
  protected static final ObjectMapper MAPPER = new ObjectMapper();

  protected static final String ID = "id";
  protected static final String ROLE = "role";
  protected static final String NAME = "name";
  protected static final String TYPE = "type";
  protected static final String USER = "user";
  protected static final String TOOL = "tool";
  protected static final String TOOLS = "tools";
  protected static final String MODEL = "model";
  protected static final String ERROR = "error";
  protected static final String DELTA = "delta";
  protected static final String INDEX = "index";
  protected static final String OBJECT = "object";
  protected static final String CALL_0 = "call_0";
  protected static final String STREAM = "stream";
  protected static final String SYSTEM = "system";
  protected static final String CHOICES = "choices";
  protected static final String CONTENT = "content";
  protected static final String MESSAGE = "message";
  protected static final String MESSAGES = "messages";
  protected static final String FUNCTION = "function";
  protected static final String ARGUMENTS = "arguments";
  protected static final String ASSISTANT = "assistant";
  protected static final String TOOL_CALLS = "tool_calls";
  protected static final String PARAMETERS = "parameters";
  protected static final String DESCRIPTION = "description";
  protected static final String TOOL_CALL_ID = "tool_call_id";

  /**
   * Builds a final (non-partial, turn-complete) {@link LlmResponse} wrapping the given text.
   *
   * @param fullText the complete response text
   * @return an {@link LlmResponse} with {@code partial=false} and {@code turnComplete=true}
   */
  protected static LlmResponse finalTextResponse(String fullText) {
    List<Part> parts = List.of(Part.fromText(fullText));
    return LlmResponse.builder() //
        .content(content(parts)) //
        .partial(false) //
        .turnComplete(true) //
        .build();
  }

  /**
   * Creates a {@link Content} with the {@code model} role wrapping the supplied parts.
   *
   * @param parts the content parts (text, function calls, etc.)
   * @return a new {@link Content} instance
   */
  protected static Content content(List<Part> parts) {
    return Content.builder().role(MODEL).parts(parts).build();
  }

  /**
   * Parses a JSON string into a {@code Map<String, Object>} suitable for use as
   * function-call arguments.
   *
   * @param json the JSON string to parse (must be a JSON object)
   * @return the parsed argument map
   * @throws Exception if the JSON is malformed
   */
  @SuppressWarnings("unchecked")
  protected static Map<String, Object> parseJsonArgs(String json) throws Exception {
    return MAPPER.readValue(json, Map.class);
  }
}
