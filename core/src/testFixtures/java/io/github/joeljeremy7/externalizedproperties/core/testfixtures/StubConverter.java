package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/** A stub {@link Converter} implementation. */
public class StubConverter<T> implements Converter<T> {
  private final Predicate<Class<?>> canConvertToDelegate;
  private final ConvertDelegate<T> convertDelegate;
  private final Map<ConverterResultKey, T> trackedConversionResults = new HashMap<>();

  /** Constructor. */
  public StubConverter() {
    // Always skips.
    this(targetType -> true, (pm, value, targetType) -> ConversionResult.skip());
  }

  /**
   * Constructor.
   *
   * @param convertDelegate The {@link #convert(InvocationContext, String, Type)} delegate.
   */
  public StubConverter(ConvertDelegate<T> convertDelegate) {
    this(targetType -> true, convertDelegate);
  }

  /**
   * Constructor.
   *
   * @param canConvertToDelegate The {@link #canConvertTo(Class)} delegate.
   * @param convertDelegate The {@link #convert(InvocationContext, String, Type)} delegate.
   */
  public StubConverter(
      Predicate<Class<?>> canConvertToDelegate, ConvertDelegate<T> convertDelegate) {
    this.canConvertToDelegate = canConvertToDelegate;
    this.convertDelegate = convertDelegate;
  }

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return canConvertToDelegate.test(targetType);
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<T> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    ConversionResult<T> result = convertDelegate.convert(context, valueToConvert, targetType);

    if (result != ConversionResult.skip()) {
      trackedConversionResults.putIfAbsent(
          new ConverterResultKey(valueToConvert, targetType), result.value());
    }
    return result;
  }

  /**
   * Get the tracked conversion results that were returned by this converter.
   *
   * @return The tracked conversion results that were returned by this converter.
   */
  public Map<ConverterResultKey, T> conversionResults() {
    return Collections.unmodifiableMap(trackedConversionResults);
  }

  /** Delegate for {@link Converter#convert(InvocationContext, String, Type)}. */
  public static interface ConvertDelegate<T> {
    /**
     * Convert value.
     *
     * @param context The invocation context.
     * @param valueToConvert The value to convert.
     * @param targetType The target type of the conversion.
     * @return The conversion result.
     */
    ConversionResult<T> convert(InvocationContext context, String valueToConvert, Type targetType);
  }

  /** The key used to track the conversion results that were returned by the stub converter. */
  public static class ConverterResultKey {
    private final String valueToConvert;
    private final Type targetType;

    /**
     * Constructor.
     *
     * @param valueToConvert The value to convert.
     * @param targetType The target type of the conversion.
     */
    public ConverterResultKey(String valueToConvert, Type targetType) {
      this.valueToConvert = valueToConvert;
      this.targetType = targetType;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
      return Objects.hash(valueToConvert, targetType);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof ConverterResultKey)) {
        return false;
      }

      ConverterResultKey other = (ConverterResultKey) obj;
      return Objects.equals(valueToConvert, other.valueToConvert)
          && Objects.equals(targetType, other.targetType);
    }
  }
}
