package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link Converter} implementation.
 * This delegates to a configured collection of {@link ConversionHandler}s.
 */
public class InternalConverter implements Converter {

    private final ClassValue<List<ConversionHandler<?>>> conversionHandlerByTargetType;
    
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
        requireNonNull(conversionHandlers, "conversionHandlers");

        this.conversionHandlerByTargetType = new ClassValue<List<ConversionHandler<?>>>() {
            @Override
            protected List<ConversionHandler<?>> computeValue(Class<?> targetType) {
                List<ConversionHandler<?>> supportsTargetType = new ArrayList<>();
                for (ConversionHandler<?> handler : conversionHandlers) {
                    if (handler.canConvertTo(targetType)) {
                        supportsTargetType.add(handler);
                    }
                }
                return supportsTargetType;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(ConversionContext context) {
        requireNonNull(context, "context");

        // No conversion needed since target type is string.
        Class<?> rawTargetType = context.rawTargetType();
        if (String.class.equals(rawTargetType)) {
            return context.value();
        }
        
        List<ConversionHandler<?>> conversionHandlers = 
            conversionHandlerByTargetType.get(rawTargetType);

        try {
            for (ConversionHandler<?> conversionHandler : conversionHandlers) {
                ConversionResult<?> result = conversionHandler.convert(context);
                if (skipped(result)) {
                    continue;
                }
                return result.value();
            }

            throw new ConversionException(String.format(
                "No converter found to convert value to target type: %s.",
                rawTargetType.getName()
            ));
        }
        catch (ConversionException cex) {
            throw cex;
        } 
        catch (Exception ex) {
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
    }

    // Skip conversion result is a singleton, cache it here to avoid internal casting.
    private static final ConversionResult<?> SKIP_RESULT = ConversionResult.skip();
    private static boolean skipped(ConversionResult<?> result) {
        return result == SKIP_RESULT;
    }
}
