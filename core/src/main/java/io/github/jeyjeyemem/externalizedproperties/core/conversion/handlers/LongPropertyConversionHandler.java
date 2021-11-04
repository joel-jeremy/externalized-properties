package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Supports conversion of resolved properties to a {@link Long} or to a primitive {@code long}.
 */
public class LongPropertyConversionHandler implements ResolvedPropertyConversionHandler<Long> {
    
    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return Long.class.equals(expectedType) ||
            Long.TYPE.equals(expectedType);
    }

    /** {@inheritDoc} */
    @Override
    public Long convert(ResolvedPropertyConversionContext context) {
        requireNonNull(context, "context");

        try {
            return Long.parseLong(context.resolvedProperty().value());
        } catch (Exception ex) {
            throw new ResolvedPropertyConversionException(String.format(
                    "Failed to convert property %s to a Long. Property value: %s",
                    context.resolvedProperty().name(),
                    context.resolvedProperty().value()
                ),  
                ex
            );
        }
    }
    
}
