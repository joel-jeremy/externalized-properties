package io.github.joeljeremy.externalizedproperties.core.internal.conversion;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.conversion.Delimiter;
import io.github.joeljeremy.externalizedproperties.core.conversion.StripEmptyValues;
import java.util.Arrays;
import java.util.regex.Pattern;

/** Tokenizer that splits values using a specified delimiter. */
public class Tokenizer {

  private final String delimiter;

  /**
   * Constructor.
   *
   * @param delimiter The default delimiter to use. The delimiter value will be quoted via {@link
   *     Pattern#quote(String)}.
   */
  public Tokenizer(String delimiter) {
    this.delimiter = Pattern.quote(requireNonNull(delimiter, "delimiter"));
  }

  /**
   * Split the string value to an array based on the determined delimiter.
   *
   * @param context The invocation context.
   * @param value The value to tokenize/split.
   * @return The resulting array.
   */
  public String[] tokenizeValue(InvocationContext context, String value) {
    String delimiterToUse = determineDelimiter(context);
    String[] tokens = value.split(delimiterToUse);
    return stripEmptyValuesIfNecessary(context, tokens);
  }

  private String determineDelimiter(InvocationContext context) {
    Delimiter delimiterOverride = context.method().findAnnotation(Delimiter.class).orElse(null);

    return delimiterOverride != null ? Pattern.quote(delimiterOverride.value()) : delimiter;
  }

  private String[] stripEmptyValuesIfNecessary(InvocationContext context, String[] tokens) {
    if (context.method().hasAnnotation(StripEmptyValues.class)) {
      // Filter empty values.
      return Arrays.stream(tokens).filter(v -> !v.isEmpty()).toArray(String[]::new);
    }
    return tokens;
  }
}
