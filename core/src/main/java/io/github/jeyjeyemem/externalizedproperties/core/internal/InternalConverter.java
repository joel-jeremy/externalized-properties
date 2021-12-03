package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.PropertyMethodConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ConversionException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.utils.TypeUtilities;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

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
    public Object convert(
            String value,
            Type expectedType
    ) {
        requireNonNull(value, "value");
        requireNonNull(expectedType, "expectedType");

        Class<?> rawExpectedType = TypeUtilities.getRawType(expectedType);

        try {
            for (ConversionHandler<?> handler : conversionHandlers) {
                if (handler.canConvertTo(rawExpectedType)) {
                    return handler.convert(new ConversionContext(
                        this, 
                        value, 
                        expectedType
                    ));
                }
            }
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Exception occurred while converting value to expected type: %s. " + 
                    "Value: %s. ",
                    rawExpectedType.getName(),
                    value
                ),
                ex
            );
        }

        throw new ConversionException(String.format(
            "No converter found to convert value to expected type: %s. Value: %s.",
            rawExpectedType.getName(),
            value
        ));
    }

    /** {@inheritDoc} */
    @Override
    public Object convert(
            ExternalizedPropertyMethodInfo externalizedPropertyMethodInfo, 
            String value,
            Type expectedType
    ) {
        requireNonNull(
            externalizedPropertyMethodInfo, 
            "externalizedPropertyMethodInfo"
        );
        requireNonNull(value, "value");
        requireNonNull(expectedType, "expectedType");

        Class<?> rawExpectedType = TypeUtilities.getRawType(expectedType);

        try {
            for (ConversionHandler<?> handler : conversionHandlers) {
                if (handler.canConvertTo(rawExpectedType)) {
                    return handler.convert(new PropertyMethodConversionContext(
                        this, 
                        externalizedPropertyMethodInfo, 
                        value,
                        expectedType
                    ));
                }
            }
        } catch (Exception ex) {
            throw new ConversionException(
                String.format(
                    "Exception occurred while converting value to expected type: %s. " + 
                    "Value: %s. Externalized property method: %s",
                    rawExpectedType.getName(),
                    value,
                    externalizedPropertyMethodInfo
                ),
                ex
            );
        }

        throw new ConversionException(String.format(
            "No converter found to convert value to expected type: %s. Value: %s." +
            "Externalized property method: %s",
            rawExpectedType.getName(),
            value,
            externalizedPropertyMethodInfo
        ));
    }
    
}
