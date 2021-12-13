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

    private final List<ConversionHandler<?>> defaultConversionHandlers;

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
        defaultConversionHandlers = Arrays.asList(
            new PrimitiveConversionHandler(),
            new ListConversionHandler(),
            new ArrayConversionHandler(),
            new OptionalConversionHandler()
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return defaultConversionHandlers.stream().anyMatch(c -> c.canConvertTo(targetType));
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        ConversionHandler<?> converter = getConversionHandler(context);
        return converter.convert(context);
    }

    private ConversionHandler<?> getConversionHandler(ConversionContext context) {
        Class<?> rawTargetType = context.rawTargetType();
        for (ConversionHandler<?> handler : defaultConversionHandlers) {
            if (handler.canConvertTo(rawTargetType)) {
                return handler;
            }
        }

        throw new ConversionException(
            "No applicable conversion handler found to convert to " + 
            context.targetType() + "."
        );
    }
}
