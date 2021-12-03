package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Default property converter which delegates to the following conversion handlers:
 * <ul>
 *  <li>{@link PrimitiveConversionHandler}</li>
 *  <li>{@link CollectionConversionHandler}</li>
 *  <li>{@link ArrayConversionHandler}</li>
 *  <li>{@link OptionalConversionHandler}</li>
 * </ul>
 */
public class DefaultConversionHandler implements ConversionHandler<Object> {

    private final List<ConversionHandler<?>> defaultConversionHandlers;

    /**
     * Constructs a {@link DefaultConversionHandler} instance 
     * which delegates to the following converters:
     * <ul>
     *  <li>{@link PrimitiveConversionHandler}</li>
     *  <li>{@link CollectionConversionHandler}</li>
     *  <li>{@link ArrayConversionHandler}</li>
     *  <li>{@link OptionalConversionHandler}</li>
     * </ul>
     */
    public DefaultConversionHandler() {
        defaultConversionHandlers = Arrays.asList(
            new PrimitiveConversionHandler(),
            new CollectionConversionHandler(),
            new ArrayConversionHandler(),
            new OptionalConversionHandler()
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return defaultConversionHandlers.stream().anyMatch(c -> c.canConvertTo(expectedType));
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        ConversionHandler<?> converter = getConversionHandler(context);
        return converter.convert(context);
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(PropertyMethodConversionContext context) {
        requireNonNull(context, "context");

        ConversionHandler<?> converter = getConversionHandler(context);
        return converter.convert(context);
    }

    private ConversionHandler<?> getConversionHandler(
            ConversionContext context
    ) {
        Class<?> rawExpectedType = context.rawExpectedType();
        for (ConversionHandler<?> handler : defaultConversionHandlers) {
            if (handler.canConvertTo(rawExpectedType)) {
                return handler;
            }
        }

        throw new ConversionException(
            "No applicable conversion handler found to convert to " + 
            context.expectedType() + "."
        );
    }
    
}
