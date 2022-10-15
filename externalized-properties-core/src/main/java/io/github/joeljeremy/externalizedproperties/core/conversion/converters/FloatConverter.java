package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;

/** Supports conversion of values to a float/Float. */
public class FloatConverter implements Converter<Float> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return float.class.equals(targetType) || Float.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Float> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(Float.parseFloat(valueToConvert));
  }
}
