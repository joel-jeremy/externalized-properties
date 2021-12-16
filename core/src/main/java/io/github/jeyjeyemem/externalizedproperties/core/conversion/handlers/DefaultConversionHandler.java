package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

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
    private final ClassValue<ConversionHandler<?>> conversionHandlersByTargetType;

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
        final List<ConversionHandler<?>> defaultConversionHandlers = Arrays.asList(
            new PrimitiveConversionHandler(),
            new ListConversionHandler(),
            new ArrayConversionHandler(),
            new OptionalConversionHandler()
        );

        conversionHandlersByTargetType = new ClassValue<ConversionHandler<?>>() {
            @Override
            protected ConversionHandler<?> computeValue(Class<?> targetType) {
                for (ConversionHandler<?> conversionHandler : defaultConversionHandlers) {
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
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> rawTargetType = context.rawTargetType();
        ConversionHandler<?> conversionHandler = conversionHandlersByTargetType.get(rawTargetType);
        if (conversionHandler == null) {
            throw new ConversionException(String.format(
                "No applicable conversion handler found to convert to %s type.", 
                context.targetType()
            ));
        }

        return conversionHandler.convert(context);
    }
}
