package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ConverterProvider;
import io.github.jeyjeyemem.externalizedproperties.core.TypeUtilities;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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

    private final ClassValue<Converter<?>> convertersByTargetType;

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
        // In order.
        final List<Converter<?>> defaultHandlers = 
            Arrays.asList(
                new PrimitiveConverter(),
                new ListConverter(rootConverter),
                new SetConverter(rootConverter),
                new ArrayConverter(rootConverter),
                new OptionalConverter(rootConverter),
                new EnumConverter(),
                new DateTimeConverter()
            );

        convertersByTargetType = new ClassValue<Converter<?>>() {
            @Override
            protected @Nullable Converter<?> computeValue(Class<?> targetType) {
                for (Converter<?> converter : defaultHandlers) {
                    if (converter.canConvertTo(targetType)) {
                        return converter;
                    }
                }
                return null;
            }
        };
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
    public ConversionResult<?> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        Class<?> rawTargetType = TypeUtilities.getRawType(targetType);
        Converter<?> converter = convertersByTargetType.get(rawTargetType);
        if (converter == null) {
            return ConversionResult.skip();
        }

        return converter.convert(
            proxyMethod,
            valueToConvert,
            targetType
        );
    }
}
