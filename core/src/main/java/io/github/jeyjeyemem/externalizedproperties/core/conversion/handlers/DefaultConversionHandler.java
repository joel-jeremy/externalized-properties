package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;

import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Default property converter which delegates to the following conversion handlers 
 * (in order):
 * <ol>
 *  <li>{@link PrimitiveConversionHandler}</li>
 *  <li>{@link ListConversionHandler}</li>
 *  <li>{@link ArrayConversionHandler}</li>
 *  <li>{@link OptionalConversionHandler}</li>
 * </ol>
 */
public class DefaultConversionHandler implements ConversionHandler<Object> {

    // private final List<ConversionHandler<?>> defaultConversionHandlers;
    private final ClassValue<ConversionHandler<Object>> conversionHandlersByTargetType;

    /**
     * Constructs a {@link DefaultConversionHandler} instance 
     * which delegates to the following conversion handlers (in order):
     * <ol>
     *  <li>{@link PrimitiveConversionHandler}</li>
     *  <li>{@link ListConversionHandler}</li>
     *  <li>{@link ArrayConversionHandler}</li>
     *  <li>{@link OptionalConversionHandler}</li>
     * </ol>
     */
    public DefaultConversionHandler() {
        // In order.
        final List<ConversionHandler<?>> defaultHandlers = 
            Arrays.asList(
                new PrimitiveConversionHandler(),
                new ListConversionHandler(),
                new ArrayConversionHandler(),
                new OptionalConversionHandler()
            );

        conversionHandlersByTargetType = new ClassValue<ConversionHandler<Object>>() {
            @Override
            protected ConversionHandler<Object> computeValue(Class<?> targetType) {
                for (ConversionHandler<?> conversionHandler : defaultHandlers) {
                    if (conversionHandler.canConvertTo(targetType)) {
                        @SuppressWarnings("unchecked")
                        ConversionHandler<Object> casted = 
                            (ConversionHandler<Object>)conversionHandler;
                        return casted;
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
    public ConversionResult<Object> convert(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> rawTargetType = context.rawTargetType();
        ConversionHandler<Object> conversionHandler = 
            conversionHandlersByTargetType.get(rawTargetType);
        if (conversionHandler == null) {
            return ConversionResult.skip();
        }

        return conversionHandler.convert(context);
    }
}
