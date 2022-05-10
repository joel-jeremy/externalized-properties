package io.github.joeljeremy7.externalizedproperties.core.conversion.converters;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

/**
 * Default property converter which delegates to the following converters 
 * (in order):
 * <ol>
 *  <li>{@link PrimitiveConverter}</li>
 *  <li>{@link ListConverter}</li>
 *  <li>{@link ArrayConverter}</li>
 *  <li>{@link OptionalConverter}</li>
 *  <li>{@link EnumConverter}</li>
 *  <li>{@link DateTimeConverter}</li>
 * </ol>
 */
public class DefaultConverter implements Converter<Object> {

    private final ClassValue<Converter<Object>> convertersByTargetType;

    /**
     * Constructs a {@link DefaultConverter} instance 
     * which delegates to the following converters (in order):
     * <ol>
     *  <li>{@link PrimitiveConverter}</li>
     *  <li>{@link ListConverter}</li>
     *  <li>{@link SetConverter}</li>
     *  <li>{@link ArrayConverter}</li>
     *  <li>{@link OptionalConverter}</li>
     *  <li>{@link EnumConverter}</li>
     *  <li>{@link DateTimeConverter}</li>
     * </ol>
     * 
     * @param rootConverter The root converter.
     */
    public DefaultConverter(Converter<?> rootConverter) {
        convertersByTargetType = new ConvertersByTargetType(
            // In order.
            Arrays.asList(
                new PrimitiveConverter(),
                new ListConverter(rootConverter),
                new SetConverter(rootConverter),
                new ArrayConverter(rootConverter),
                new OptionalConverter(rootConverter),
                new EnumConverter(),
                new DateTimeConverter()
            )
        );
    }

    /**
     * The {@link ConverterProvider} for {@link DefaultConverter}.
     * 
     * @return The {@link ConverterProvider} for {@link DefaultConverter}.
     */
    public static ConverterProvider<DefaultConverter> provider() {
        return (externalizedProperties, rootConverter) -> 
            new DefaultConverter(rootConverter);
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        if (targetType == null) {
            return false;
        }
        return convertersByTargetType.get(targetType) != null;
    }
    
    /** {@inheritDoc} */
    @Override
    public ConversionResult<Object> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        Class<?> rawTargetType = TypeUtilities.getRawType(targetType);
        Converter<Object> converter = convertersByTargetType.get(rawTargetType);
        if (converter == null) {
            return ConversionResult.skip();
        }

        return converter.convert(
            proxyMethod,
            valueToConvert,
            targetType
        );
    }

    /**
     * Maps a {@link Converter} instances to target types.
     */
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
                    Converter<Object> casted = (Converter<Object>)converter;
                    return casted;
                }
            }
            return null;
        }
    }
}
