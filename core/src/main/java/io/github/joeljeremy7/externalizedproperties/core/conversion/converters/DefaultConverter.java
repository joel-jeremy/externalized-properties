package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Default property converter which delegates to the following converters (in order):
 *
 * <ol>
 *   <li>{@link IntegerConverter}
 *   <li>{@link BooleanConverter}
 *   <li>{@link LongConverter}
 *   <li>{@link ShortConverter}
 *   <li>{@link FloatConverter}
 *   <li>{@link DoubleConverter}
 *   <li>{@link CharacterConverter}
 *   <li>{@link ByteConverter}
 *   <li>{@link ListConverter}
 *   <li>{@link ArrayConverter}
 *   <li>{@link SetConverter}
 *   <li>{@link EnumConverter}
 *   <li>{@link DateTimeConverter}
 * </ol>
 */
public class DefaultConverter implements Converter<Object> {

  private final ClassValue<Converter<Object>> convertersByTargetType;

  /**
   * Constructs a {@link DefaultConverter} instance which delegates to the following converters (in
   * order):
   *
   * <ol>
   *   <li>{@link IntegerConverter}
   *   <li>{@link BooleanConverter}
   *   <li>{@link LongConverter}
   *   <li>{@link ShortConverter}
   *   <li>{@link FloatConverter}
   *   <li>{@link DoubleConverter}
   *   <li>{@link CharacterConverter}
   *   <li>{@link ByteConverter}
   *   <li>{@link ListConverter}
   *   <li>{@link ArrayConverter}
   *   <li>{@link SetConverter}
   *   <li>{@link EnumConverter}
   *   <li>{@link DateTimeConverter}
   * </ol>
   */
  public DefaultConverter() {
    convertersByTargetType =
        new ConvertersByTargetType(
            // In order.
            Arrays.asList(
                new IntegerConverter(),
                new BooleanConverter(),
                new LongConverter(),
                new ShortConverter(),
                new FloatConverter(),
                new DoubleConverter(),
                new CharacterConverter(),
                new ByteConverter(),
                new ListConverter(),
                new ArrayConverter(),
                new SetConverter(),
                new EnumConverter(),
                new DateTimeConverter()));
  }

  /** {@inheritDoc} */
  @Override
  public boolean canConvertTo(Class<?> targetType) {
    return convertersByTargetType.get(targetType) != null;
  }

  /** {@inheritDoc} */
  @Override
  public ConversionResult<Object> convert(
      InvocationContext context, String valueToConvert, Type targetType) {
    Class<?> rawTargetType = TypeUtilities.getRawType(targetType);
    Converter<Object> converter = convertersByTargetType.get(rawTargetType);
    if (converter == null) {
      return ConversionResult.skip();
    }

    return converter.convert(context, valueToConvert, targetType);
  }

  /** Maps a {@link Converter} instances to target types. */
  private static class ConvertersByTargetType extends ClassValue<Converter<Object>> {

    private final Collection<Converter<?>> defaultConverters;

    /**
     * Constructor.
     *
     * @param defaultConverters The default converters.
     */
    ConvertersByTargetType(Collection<Converter<?>> defaultConverters) {
      this.defaultConverters = defaultConverters;
    }

    /**
     * This method will return a {@link Converter} instance based on the specified target type.
     *
     * @param targetType The target type to convert to.
     * @return A {@link Converter} instance which support conversion to the target type.
     */
    @Override
    protected @Nullable Converter<Object> computeValue(Class<?> targetType) {
      for (Converter<?> converter : defaultConverters) {
        if (converter.canConvertTo(targetType)) {
          @SuppressWarnings("unchecked")
          Converter<Object> casted = (Converter<Object>) converter;
          return casted;
        }
      }
      return null;
    }
  }
}
