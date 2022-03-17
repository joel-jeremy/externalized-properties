package io.github.jeyjeyemem.externalizedproperties.core.conversion.converters;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;

import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Default property converter which delegates to the following conversion handlers 
 * (in order):
 * <ol>
 *  <li>{@link PrimitiveConverter}</li>
 *  <li>{@link ListConverter}</li>
 *  <li>{@link ArrayConverter}</li>
 *  <li>{@link OptionalConverter}</li>
 * </ol>
 */
public class DefaultConverter implements Converter<Object> {

    // private final List<ConversionHandler<?>> defaultConversionHandlers;
    private final ClassValue<Converter<?>> conversionHandlersByTargetType;

    /**
     * Constructs a {@link DefaultConverter} instance 
     * which delegates to the following conversion handlers (in order):
     * <ol>
     *  <li>{@link PrimitiveConverter}</li>
     *  <li>{@link ListConverter}</li>
     *  <li>{@link ArrayConverter}</li>
     *  <li>{@link OptionalConverter}</li>
     * </ol>
     */
    public DefaultConverter() {
        // In order.
        final List<Converter<?>> defaultHandlers = 
            Arrays.asList(
                new PrimitiveConverter(),
                new ListConverter(),
                new ArrayConverter(),
                new OptionalConverter()
            );

        conversionHandlersByTargetType = new ClassValue<Converter<?>>() {
            @Override
            protected Converter<?> computeValue(Class<?> targetType) {
                for (Converter<?> conversionHandler : defaultHandlers) {
                    if (conversionHandler.canConvertTo(targetType)) {
                        return conversionHandler;
                    }
                }
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        if (targetType == null) return false;
        return conversionHandlersByTargetType.get(targetType) != null;
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> rawTargetType = context.rawTargetType();
        Converter<?> conversionHandler = 
            conversionHandlersByTargetType.get(rawTargetType);
        if (conversionHandler == null) {
            return ConversionResult.skip();
        }

        return conversionHandler.convert(context);
    }
}
