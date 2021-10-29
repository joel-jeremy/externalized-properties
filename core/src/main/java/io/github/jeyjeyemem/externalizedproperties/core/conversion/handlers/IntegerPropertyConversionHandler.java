package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to an {@link Integer} or to a primitive {@code int}.
 */
public class IntegerPropertyConversionHandler implements ResolvedPropertyConversionHandler<Integer> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Integer.class.equals(expectedType) ||
            Integer.TYPE.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Integer convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        try {
            return Integer.parseInt(context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to an Integer. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }
    
}
