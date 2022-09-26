package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;
import java.math.BigInteger;

/** Supports conversion of values to a {@link BigInteger}. */
public class BigIntegerConverter implements Converter<BigInteger> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return BigInteger.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<BigInteger> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(new BigInteger(valueToConvert));
  }
}
