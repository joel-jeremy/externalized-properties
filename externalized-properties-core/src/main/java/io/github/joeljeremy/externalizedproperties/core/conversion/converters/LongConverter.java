package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;

/** Supports conversion of values to a long/Long. */
public class LongConverter implements Converter<Long> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return long.class.equals(targetType) || Long.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Long> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(Long.parseLong(valueToConvert));
  }
}
