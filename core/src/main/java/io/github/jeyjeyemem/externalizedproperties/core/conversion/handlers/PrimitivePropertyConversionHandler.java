package io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandlerContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ResolvedPropertyConversionException;

import java.util.Arrays;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Primitive property converter which delegates to the following converters:
 * <ul>
 *  <li>{@link IntegerPropertyConversionHandler}</li>
 *  <li>{@link LongPropertyConversionHandler}</li>
 *  <li>{@link FloatPropertyConversionHandler}</li>
 *  <li>{@link DoublePropertyConversionHandler}</li>
 * </ul>
 */
public class PrimitivePropertyConversionHandler implements ResolvedPropertyConversionHandler<Object> {
    private final List<ResolvedPropertyConversionHandler<?>> primitiveConverters;

    /**
     * Constructs a {@link PrimitivePropertyConversionHandler} instance 
     * which delegates to the following primitive converters:
     * <ul>
     *  <li>{@link IntegerPropertyConversionHandler}</li>
     *  <li>{@link LongPropertyConversionHandler}</li>
     *  <li>{@link FloatPropertyConversionHandler}</li>
     *  <li>{@link DoublePropertyConversionHandler}</li>
     * </ul>
     */
    public PrimitivePropertyConversionHandler() {
        primitiveConverters = Arrays.asList(
            new IntegerPropertyConversionHandler(),
            new LongPropertyConversionHandler(),
            new FloatPropertyConversionHandler(),
            new DoublePropertyConversionHandler()
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> expectedType) {
        return primitiveConverters.stream().anyMatch(c -> c.canConvertTo(expectedType));
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ResolvedPropertyConversionHandlerContext context) {
        requireNonNull(context, "context");

        ResolvedPropertyConversionHandler<?> converter = primitiveConverters.stream()
            .filter(c -> c.canConvertTo(context.expectedType()))
            .findFirst()
            // This should neven happen since infrastructure checks with 
            // canConvertTo(...) method before calling convertValue(...) method.
            .orElseThrow(() -> new ResolvedPropertyConversionException(
                "No applicable conversion handler found to convert to " + 
                context.expectedType() + "."
            ));

        return converter.convert(context);
    }
    
}
