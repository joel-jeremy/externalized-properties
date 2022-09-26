package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;

/** Supports conversion of values to an int/Integer. */
public class IntegerConverter implements Converter<Integer> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return int.class.equals(targetType) || Integer.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Integer> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(Integer.parseInt(valueToConvert));
  }
}
