package com.github.svetanis.models.spi.prefix;

import com.github.svetanis.models.spi.ModelProvider;

/**
 * Extension of {@link ModelProvider} that formalises the {@code provider/model}
 * prefix convention and provides automatic prefix stripping.
 * 
 * <h2>Problem solved</h2>
 * <p>Every {@link ModelProvider} implementation must strip its own prefix before
 * passing the model name to the underlying API (e.g. {@code "groq/llama3}
 * must become {@code "llama3"} in the HTTP request). Without this interface,
 * each provider author must remember to do the stripping manually - boilerplate
 * that is easy to forget and leads to silent runtime failures.
 * 
 * <h2>Usage</h2>
 * <p>Implement this interface (or extend {@link AbstractPrefixAwareModelProvider})
 * instead of {@link ModelProvider}. The default {@link #stripPrefix(String)} method
 * handles the stripping, and {@link AbstractPrefixAwareModelProvider} goes further
 * by calling it automatically so implementations never touch raw prefixed names.
 * 
 * <pre>
 * public class GroqModelProvider extends AbstractPrefixAwareModelProvider {
 *    public GroqModelProvider() {super("groq/*);}
 *    
 *    {@literal @}Override
 *    protected BaseLlm createFromBareModelName(String bareModelName){
 *      return new OpenAiCompatibleLlm(bareModelName, API_URL, System.getenv("GROQ_API_KEY"));
 *    }
 * }
 * </pre>
 * 
 */



public interface PrefixAwareModelProvider extends ModelProvider {

  /**
   * Returns the provider prefix including the trailing slash, e.g. {@code "groq/"}
   * 
   *  <p>Used by {@link #stripPrefix(String)| and by {@link AbstractPrefixAwareModelProvider}
   *  to derive {@link #modelPattern()} automatically.
   */
  String prefix();

  
  /**
   * Strips {@link #prefix()} from {@code modelName} if present.
   * 
   *  <p>The result is the bare model identifier expected by the provider's API.
   *  Defensive: returns {@code modelName} unchanged if the prefix is absent,
   *  so the method is safe to call regardless of whether the name has already 
   *  been stripped.
   *  
   *  @param modelName the full model name, e.g. {@code "groq/llama3}
   *  @return the bare model name, e.g. {@code "llama3"}
   */
  default String stripPrefix(String modelName) {
    String prefix = prefix();
    return modelName.startsWith(prefix) ? modelName.substring(prefix.length()) : modelName;
  }
}
