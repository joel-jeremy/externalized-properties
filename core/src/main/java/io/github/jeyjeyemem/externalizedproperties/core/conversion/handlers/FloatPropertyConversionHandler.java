package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to a {@link Float} or to a primitive {@code float}.
 */
public class FloatPropertyConversionHandler implements ResolvedPropertyConversionHandler<Float> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Float.class.equals(expectedType) ||
            Float.TYPE.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Float convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        try {
            return Float.parseFloat(context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to a Float. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }
    
}
