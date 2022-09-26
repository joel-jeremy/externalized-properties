package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import java.lang.reflect.Type;

/** Supports conversion of values to a char/Character. */
public class CharacterConverter implements Converter<Character> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return char.class.equals(targetType) || Character.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Character> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    if (valueToConvert.length() != 1) {
      throw new ConversionException("Invalid char value: " + valueToConvert);
    }
    return ConversionResult.of(valueToConvert.charAt(0));
  }
}
