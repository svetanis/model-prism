package com.github.svetanis.models.spi.prefix;

import org.apache.commons.lang3.StringUtils;

import com.google.adk.models.BaseLlm;

/**
 * Convenience base class for {@link PrefixAwareModelProvider} implementations.
 * 
 * <h2>What this eliminates</h2>
 * <p>Providers that extend this class need implement only one method - 
 * {@link #createFromBareModelName(String)} - and receive the base model identifier
 * with no prefix. All boilerplate ({@link #prefix()}, {@link #modelPattern()},
 * and prefix stripping inside {@link #create(String)}) is handled here.
 * 
 * <h2>Before (plain {@code ModelProvider})</h2>
 * <pre>
 * public class GroqModelProvider implements ModelProvider {
 *    private static final String PREFIX = "groq/";
 *    
 *    {@literal @}Override public String modelPattern() {return "groq/.*"; }
 *    
 *    {@literal @}Override
 *    public BaseLlm create(String modelName){
 *      // must remember to strip - silent runtime failure if forgotten
 *      String bare = modelName.startWith(PREFIX) ? modelName.substring(PREFIX.length()) : modelName;
 *      return new OpenAiCompatible(bare, API_URL, System.getenv("GROQ_API_KEY"));
 *    }
 * }
 * </pre>
 * 
 * <h2>After (extending this class)</h2>
 * <pre>
 * public class GroqModelProvider extends AbstractPrefixAwareModelProvider {
 *    public GroqModelProvider() { super("groq/"); }
 *    
 *    {@literal @}Override
 *    protected BaseLlm createFromBareModelName(String bareModelName) {
 *       return new OpenAiCompatible(bareModelName, API_URL, System.getenv("GROQ_API_KEY"));
 *    }
 * }
 * </pre>
 */


public abstract class AbstractPrefixAwareModelProvider implements PrefixAwareModelProvider {

  private final String prefix;
  
  /**
   * @param prefix the provider prefix including the trailing slash, e.g. {@code "groq/"} 
   */
  protected AbstractPrefixAwareModelProvider(String prefix) {
    if(StringUtils.isNotBlank(prefix)) {
      throw new IllegalArgumentException("prefix can not be blank");
    }
    this.prefix = prefix;
  }
  
  /**
   * Returns the prefix supplied to the constructor, e.g. {@code "groq/"}.
   */
  @Override
  public final String prefix() {
    return prefix;
  }
  
  /**
   * Derives the regex pattern from the prefix: {@code "<prefix>.*"}.
   * 
   * <p>For example, a prefix of {@code "groq/"} produces the pattern {@code "groq/."},
   * which matches any model string starting with {@code "groq/"}.
   */
  @Override
  public final String modelPattern() {
    return prefix + ".*";
  }
  
  /**
   * Strips the prefix and delegates to {@link #createFromBareModelName(String)}.
   * 
   * <p>Implementations never receive a prefixed model name - stripping is guaranteed
   * by this method before delegation.
   * 
   * @param modelName the full model name as supplied by the agent, e.g. {@code "groq/llama3"}
   */
  @Override
  public final BaseLlm create(String modelName) {
    return createFromBareModelName(stripPrefix(modelName));
  }
  
  /**
   * Creates a {@link BaseLlm} for the given bare model identifier.
   * 
   * <p>The {@code bareModelName} has already had the provider prefix removed.
   * Pass it directly to the underlying API client.
   * 
   * @param bareModelName the model name without prefix, e.g. {@code "llama3"}
   */
  protected abstract BaseLlm createFromBareModelName(String bareModelName);
}
