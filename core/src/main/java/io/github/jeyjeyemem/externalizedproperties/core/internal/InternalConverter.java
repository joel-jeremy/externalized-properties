package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.util.Arrays;
import java.util.Collection;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link Converter} implementation.
 * This delegates to a configured collection of {@link ConversionHandler}s.
 */
public class InternalConverter implements Converter {

    private final Collection<ConversionHandler<?>> conversionHandlers;

    /**
     * Constructor.
     * 
     * @param conversionHandlers The collection of {@link ConversionHandler}s
     * to handle the actual conversion.
     */
    public InternalConverter(ConversionHandler<?>... conversionHandlers) {
        this(Arrays.asList(
            requireNonNull(
                conversionHandlers,
                "conversionHandlers"
            )
        ));
    }

    /**
     * Constructor.
     * 
     * @param conversionHandlers The collection of {@link ConversionHandler}s
     * to handle the actual conversion.
     */
    public InternalConverter(Collection<ConversionHandler<?>> conversionHandlers) {
        this.conversionHandlers = requireNonNull(
            conversionHandlers, 
            "conversionHandlers"
        );
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        Class<?> rawTargetType = context.rawTargetType();

        try {
            for (ConversionHandler<?> handler : conversionHandlers) {
                if (handler.canConvertTo(rawTargetType)) {
                    return handler.convert(context);
                }
            }
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Exception occurred while converting value to target type: %s. " + 
                    "Value: %s",
                    rawTargetType.getName(),
                    context.value()
                ),
                ex
            );
        }

        throw new ConversionException(String.format(
            "No converter found to convert value to target type: %s. Value: %s",
            rawTargetType.getName(),
            context.value()
        ));
    }
}
