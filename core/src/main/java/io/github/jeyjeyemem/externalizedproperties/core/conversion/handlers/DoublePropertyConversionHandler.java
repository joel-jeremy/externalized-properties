package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to a {@link Double} or to a primitive {@code double}.
 */
public class DoublePropertyConversionHandler implements ResolvedPropertyConversionHandler<Double> {

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Double.class.equals(expectedType) ||
            Double.TYPE.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Double convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");
        
        try {
            return Double.parseDouble(context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to a Double. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }
    
}
