package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

/**
 * Supports conversion of values to a {@link Pattern}. Pattern flags may be embedded in the regex
 * pattern.
 */
public class PatternConverter implements Converter<Pattern> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return Pattern.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Pattern> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(Pattern.compile(valueToConvert));
  }
}
