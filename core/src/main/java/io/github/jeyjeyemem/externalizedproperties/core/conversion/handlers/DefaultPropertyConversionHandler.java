package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Default property converter which delegates to the following converters:
 * <ul>
 *  <li>{@link PrimitivePropertyConversionHandler}</li>
 *  <li>{@link ListPropertyConversionHandler}</li>
 *  <li>{@link ArrayPropertyConversionHandler}</li>
 *  <li>{@link OptionalPropertyConversionHandler}</li>
 * </ul>
 */
public class DefaultPropertyConversionHandler implements ResolvedPropertyConversionHandler<Object> {

    private final List<ResolvedPropertyConversionHandler<?>> defaultConversionHandlers;

    /**
     * Constructs a {@link DefaultPropertyConversionHandler} instance 
     * which delegates to the following converters:
     * <ul>
     *  <li>{@link PrimitivePropertyConversionHandler}</li>
     *  <li>{@link ListPropertyConversionHandler}</li>
     *  <li>{@link ArrayPropertyConversionHandler}</li>
     *  <li>{@link OptionalPropertyConversionHandler}</li>
     * </ul>
     */
    public DefaultPropertyConversionHandler() {
        defaultConversionHandlers = Arrays.asList(
            new PrimitivePropertyConversionHandler(),
            new ListPropertyConversionHandler(),
            new ArrayPropertyConversionHandler(),
            new OptionalPropertyConversionHandler()
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return defaultConversionHandlers.stream().anyMatch(c -> c.canConvertTo(expectedType));
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        ResolvedPropertyConversionHandler<?> converter = defaultConversionHandlers.stream()
            .filter(c -> c.canConvertTo(context.expectedType()))
            .findFirst()
            // This should neven happen since infrastructure checks with 
            // canConvertTo(...) method before calling convert(...) method.
            .orElseThrow(() -> new ResolvedPropertyConversionException(
                "No applicable conversion handler found to convert to " + 
                context.expectedType() + "."
            ));

        return converter.convert(context);
    }
    
}
