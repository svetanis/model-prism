package com.github.svetanis.models.demo.structured;

import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import jakarta.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Provider} that builds a {@link Schema} defining the structure for extracted movie data.
 *
 * <p>The schema declares the following fields:
 * <ul>
 *   <li>{@code title} (string, required) — the movie title</li>
 *   <li>{@code director} (string, required) — the director's full name</li>
 *   <li>{@code year} (integer, required) — release year</li>
 *   <li>{@code genre} (string, required) — primary genre</li>
 *   <li>{@code summary} (string, required) — one-sentence plot summary</li>
 *   <li>{@code character} (string, optional) — main character</li>
 *   <li>{@code motivation} (string, optional) — main character's motivation</li>
 * </ul>
 *
 * @see MovieExtractorProvider
 */
public final class MovieSchemaProvider implements Provider<Schema> {

  private static final String TITLE = "title";
  private static final String DIRECTOR = "director";
  private static final String YEAR = "year";
  private static final String GENRE = "genre";
  private static final String SUMMARY = "summary";
  private static final String CHARACTER = "character";
  private static final String MOTIVATION = "motivation";

  @Override
  public Schema get() {
    return Schema.builder()
        .type(new Type(Type.Known.OBJECT)) //
        .description("Structured facts extracted from a movie description") //
        .properties(properties()) //
        .required(List.of(TITLE, DIRECTOR, YEAR, GENRE, SUMMARY)) //
        .propertyOrdering(List.of(TITLE, DIRECTOR, YEAR, GENRE, SUMMARY, CHARACTER, MOTIVATION)) //
        .build();
  }

  private Map<String, Schema> properties() {
    Type stype = new Type(Type.Known.STRING);
    Type itype = new Type(Type.Known.INTEGER);
    String ydesc = "Release year as a 4 digit integer";
    String gdesc = "Primary genre (e.g. science-fiction, drama, thriller";
    Schema title = Schema.builder().type(stype).description("The moview title").build();
    Schema director = Schema.builder().type(stype).description("The director's full name").build();
    Schema year = Schema.builder().type(itype).description(ydesc).build();
    Schema genre = Schema.builder().type(stype).description(gdesc).build();
    Schema summary = Schema.builder().type(stype).description("One-sentence plot summary").build();
    Schema character = Schema.builder().type(stype).description("Main character").build();
    Schema motiv = Schema.builder().type(stype).description("Main character motivation").build();
    Map<String, Schema> map = new HashMap<>();
    map.put(TITLE, title);
    map.put(DIRECTOR, director);
    map.put(YEAR, year);
    map.put(GENRE, genre);
    map.put(SUMMARY, summary);
    map.put(CHARACTER, character);
    map.put(MOTIVATION, motiv);
    return map;
  }
}
