package io.github.joeljeremy.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Supports conversion of values to a {@link Path}. */
public class PathConverter implements Converter<Path> {

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return Path.class.equals(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Path> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    return ConversionResult.of(Paths.get(valueToConvert));
  }
}
