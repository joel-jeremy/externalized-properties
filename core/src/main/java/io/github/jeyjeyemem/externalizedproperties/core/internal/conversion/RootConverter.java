package io.github.jeyjeyemem.externalizedproperties.core.internal.conversion;

import io.github.jeyjeyemem.externalizedproperties.core.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.ConversionResult;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link Converter} implementation.
 * This delegates to a configured collection of {@link Converter}s.
 */
public class RootConverter implements Converter<Object> {

    private final ClassValue<List<Converter<?>>> convertersByTargetType;
    
    /**
     * Constructor.
     * 
     * @param converters The collection of {@link Converter}s
     * to handle the actual conversion.
     */
    public RootConverter(Converter<?>... converters) {
        this(Arrays.asList(
            requireNonNull(
                converters,
                "converters"
            )
        ));
    }

    /**
     * Constructor.
     * 
     * @param converters The collection of {@link Converter}s
     * to handle the actual conversion.
     */
    public RootConverter(Collection<Converter<?>> converters) {
        requireNonNull(converters, "converters");

        this.convertersByTargetType = new ClassValue<List<Converter<?>>>() {
            @Override
            protected List<Converter<?>> computeValue(Class<?> targetType) {
                List<Converter<?>> supportsTargetType = new ArrayList<>();
                for (Converter<?> handler : converters) {
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
    public boolean canConvertTo(Class<?> targetType) {
        return !convertersByTargetType.get(targetType).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(ConversionContext context) {
        requireNonNull(context, "context");

        // No conversion needed since target type is string.
        Class<?> rawTargetType = context.rawTargetType();
        if (String.class.equals(rawTargetType)) {
            return ConversionResult.of(context.value());
        }
        
        List<Converter<?>> converters = 
            convertersByTargetType.get(rawTargetType);

        try {
            for (Converter<?> converter : converters) {
                ConversionResult<?> result = converter.convert(context);
                if (skipped(result)) {
                    continue;
                }
                return ConversionResult.of(result.value());
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
