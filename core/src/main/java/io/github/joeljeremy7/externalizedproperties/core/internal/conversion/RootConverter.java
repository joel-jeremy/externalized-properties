package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.OptionalConverter;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The root {@link Converter}. All requests to convert properties are routed through this converter
 * and delegated to the registered {@link Converter}s.
 */
public class RootConverter implements Converter<Object> {
    private final ConvertersByTargetType convertersByTargetType;
    
    /**
     * Constructor.
     * 
     * @param converters The collection of {@link Converter}s to handle the actual conversion.
     */
    public RootConverter(Converter<?>... converters) {
        this(
            Arrays.asList(requireNonNull(converters, "converters"))
        );
    }

    /**
     * Constructor.
     * 
     * @param converters The collection of {@link Converter}s to handle the actual conversion.
     */
    public RootConverter(Collection<Converter<?>> converters) {
        this.convertersByTargetType = new ConvertersByTargetType(
            requireNonNull(converters, "converters")
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return !convertersByTargetType.get(targetType).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<Object> convert(
            ProxyMethod proxyMethod,
            String valueToConvert,
            Type targetType
    ) {
        requireNonNull(proxyMethod, "proxyMethod");
        requireNonNull(valueToConvert, "valueToConvert");
        requireNonNull(targetType, "targetType");

        // No conversion needed since target type is string.
        Class<?> rawTargetType = TypeUtilities.getRawType(targetType);
        if (String.class.equals(rawTargetType)) {
            return ConversionResult.of(valueToConvert);
        }
        
        List<Converter<Object>> converters = convertersByTargetType.get(rawTargetType);

        try {
            for (Converter<Object> converter : converters) {
                ConversionResult<Object> result = converter.convert(
                    proxyMethod,
                    valueToConvert,
                    targetType
                );

                if (skipped(result)) {
                    continue;
                }

                return result;
            }

            throw new ConversionException(String.format(
                "Conversion to target type not supported: %s. " + 
                "Please make sure a converter which supports conversion " + 
                "to the target type is registered when building %s.",
                rawTargetType.getName(),
                ExternalizedProperties.class.getSimpleName()
            ));
        }
        catch (ConversionException cex) {
            throw cex;
        } 
        catch (RuntimeException ex) {
            throw new ConversionException(
                String.format(
                    "Exception occurred while converting value to target type: %s. " + 
                    "Value: %s",
                    rawTargetType.getName(),
                    valueToConvert
                ),
                ex
            );
        }
    }

    // Skip conversion result is a singleton, cache it here to avoid internal casting.
    private static final ConversionResult<?> SKIP_RESULT = ConversionResult.skip();
    private static boolean skipped(ConversionResult<?> result) {
        return result.equals(SKIP_RESULT);
    }

    /**
     * Maps a list of {@link Converter} instances to target types.
     */
    private static class ConvertersByTargetType extends ClassValue<List<Converter<Object>>> {

        private final List<Converter<?>> registeredConverters;

        /**
         * Constructor.
         * 
         * @param registeredConverters The registered {@link ConverterProvider} instances.
         */
        ConvertersByTargetType(
                Collection<Converter<?>> registeredConverters
        ) {
            this.registeredConverters = setupOutOfTheBoxConverters(registeredConverters);
        }

        /**
         * This method will return a list of {@link Converter} instances based on the specified 
         * target type.
         * 
         * @param targetType The target type to convert to.
         * @return The list of {@link Converter} instances which support conversion to the
         * target type.
         */
        @Override
        protected List<Converter<Object>> computeValue(Class<?> targetType) {
            // We should not throw here because result of this method is
            // used in canConvertTo(...) to determine supported target types.

            List<Converter<Object>> supportsTargetType = new ArrayList<>();
            for (Converter<?> converter : registeredConverters) {
                if (converter.canConvertTo(targetType)) {
                    @SuppressWarnings("unchecked")
                    Converter<Object> casted = (Converter<Object>)converter;
                    supportsTargetType.add(casted);
                }
            }
            return supportsTargetType;
        }

        private static List<Converter<?>> setupOutOfTheBoxConverters(
                Collection<Converter<?>> original
        ) {
            List<Converter<?>> converters = new ArrayList<>(original);
            registerOptionalConverterIfNecessary(converters);
            return converters;
        }

        private static void registerOptionalConverterIfNecessary(
                List<Converter<?>> converters
        ) {
            // Add if no OptionalConverter was explicitly added.
            if (converters.stream()
                    .noneMatch(c -> c.canConvertTo(Optional.class))
            ) {
                // Optional conversion is natively supported out of the box.
                converters.add(new OptionalConverter());
            }
        }
    }
}
