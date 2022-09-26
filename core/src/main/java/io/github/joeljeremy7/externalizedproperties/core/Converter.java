package io.github.joeljeremy7.externalizedproperties.core;

import java.lang.reflect.Type;

/**
 * API for handling conversion of values to various types.
 *
 * @param <T> The target type for this converter.
 */
public interface Converter<T> {
  /**
   * Checks if the handler can convert values to the target type.
   *
   * @param targetType The target type to convert to.
   * @return {@code true}, if the implementation can convert values to the specified type.
   *     Otherwise, {@code false}.
   */
  boolean canConvertTo(Class<?> targetType);

  /**
   * Convert value to the target type.
   *
   * @implNote If implementation does not support conversion to the target type, it may return
   *     {@link ConversionResult#skip()} to indicate that the converter cannot handle conversion to
   *     the target type and that the conversion process should skip/move to the next registered
   *     converter in the conversion pipeline. However, if an exception is thrown, the conversion
   *     will fail and will not attempt to convert using the other registered converters.
   * @param context The proxy method invocation context.
   * @param valueToConvert The value to convert.
   * @return The result of conversion with the return type of the proxy method as the target type to
   *     convert to, or {@link ConversionResult#skip()} if the converter cannot handle conversion to
   *     the target type and that the conversion process should skip/move to the next registered
   *     converter in the conversion pipeline.
   */
  default ConversionResult<T> convert(InvocationContext context, String valueToConvert) {
    return convert(context, valueToConvert, context.method().returnType());
  }

  /**
   * Convert value to the target type.
   *
   * @implNote If implementation does not support conversion to the target type, it may return
   *     {@link ConversionResult#skip()} to indicate that the converter cannot handle conversion to
   *     the target type and that the conversion process should skip/move to the next registered
   *     converter in the conversion pipeline. However, if an exception is thrown, the conversion
   *     will fail and will not attempt to convert using the other registered converters.
   * @param context The invocation context.
   * @param valueToConvert The value to convert.
   * @param targetType The target type of the conversion. In the case of converter facades (see
   *     {@link ConverterFacade}), target type may be different from the invoked proxy method's
   *     return type.
   * @return The result of conversion to the target type or {@link ConversionResult#skip()} if the
   *     converter cannot handle conversion to the target type and that the conversion process
   *     should skip/move to the next registered converter in the conversion pipeline.
   */
  ConversionResult<T> convert(InvocationContext context, String valueToConvert, Type targetType);
}
