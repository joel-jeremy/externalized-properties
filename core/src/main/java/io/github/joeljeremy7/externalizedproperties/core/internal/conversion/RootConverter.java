package io.github.joeljeremy7.externalizedproperties.core.internal.conversion;

import io.github.joeljeremy7.externalizedproperties.core.ConversionResult;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterProvider;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.TypeUtilities;
import io.github.joeljeremy7.externalizedproperties.core.conversion.ConversionException;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param converterProviders The collection of {@link ConverterProvider}s
     * to provide converters that handle the actual conversion.
     */
    public RootConverter(
            ExternalizedProperties externalizedProperties, 
            ConverterProvider<?>... converterProviders
    ) {
        this(
            externalizedProperties,
            Arrays.asList(requireNonNull(converterProviders, "converterProviders"))
        );
    }

    /**
     * Constructor.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param converterProviders The collection of {@link ConverterProvider}s
     * to provide converters that handle the actual conversion.
     */
    public RootConverter(
            ExternalizedProperties externalizedProperties,
            Collection<ConverterProvider<?>> converterProviders
    ) {
        this.convertersByTargetType = new ConvertersByTargetType(
            this,
            requireNonNull(externalizedProperties, "externalizedProperties"),
            requireNonNull(converterProviders, "converterProviders")
        );
    }

    /**
     * The {@link ConverterProvider} for {@link RootConverter}.
     * 
     * @param converterProviders The registered {@link ConverterProvider}s which provide 
     * {@link Converter} instances.
     * @return The {@link ConverterProvider} for {@link RootConverter}.
     */
    public static RootConverter.Provider provider(
            ConverterProvider<?>... converterProviders
    ) {
        requireNonNull(converterProviders, "converterProviders");
        return externalizedProperties -> new RootConverter(
            externalizedProperties, 
            converterProviders
        );
    }

    /**
     * The {@link ConverterProvider} for {@link RootConverter}.
     * 
     * @param converterProviders The registered {@link ConverterProvider}s which provide 
     * {@link Converter} instances.
     * @return The {@link ConverterProvider} for {@link RootConverter}.
     */
    public static RootConverter.Provider provider(
            Collection<ConverterProvider<?>> converterProviders
    ) {
        requireNonNull(converterProviders, "converterProviders");
        return externalizedProperties -> new RootConverter(
            externalizedProperties, 
            converterProviders
        );
    }

    /** {@inheritDoc} */
    @Override
    public boolean canConvertTo(Class<?> targetType) {
        return !convertersByTargetType.get(targetType).isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ConversionResult<?> convert(
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
        
        List<Converter<?>> converters = 
            convertersByTargetType.get(rawTargetType);

        try {
            for (Converter<?> converter : converters) {
                ConversionResult<?> result = converter.convert(
                    proxyMethod,
                    valueToConvert,
                    targetType
                );

                if (skipped(result)) {
                    continue;
                }

                return ConversionResult.of(result.value());
            }

            throw new ConversionException(String.format(
                "Conversion to target type not supported: %s. " + 
                "Please make sure a converter which supports conversion " + 
                "to the target type is registered when building ExternalizedProperties.",
                rawTargetType.getName()
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
        return result == SKIP_RESULT;
    }

    /**
     * Maps target type to converter instances which supports the target type.
     */
    private static class ConvertersByTargetType extends ClassValue<List<Converter<?>>> {

        private final RootConverter rootConverter;
        private final ExternalizedProperties externalizedProperties;
        private final List<ConverterProvider<?>> registeredConverterProviders;

        /**
         * Constructor.
         * 
         * @param registeredConverterProviders The registered {@link ConverterProvider} instances.
         */
        ConvertersByTargetType(
                RootConverter rootConverter,
                ExternalizedProperties externalizedProperties,
                Collection<ConverterProvider<?>> registeredConverterProviders
        ) {
            this.rootConverter = rootConverter;
            this.externalizedProperties = externalizedProperties;
            this.registeredConverterProviders = memoizeAll(registeredConverterProviders);
        }

        /**
         * This method will return a converter instance based on the specified target type.
         * 
         * @param targetType The target type to convert to.
         * @return The list of {@link Converter} instances which support conversion to the
         * target type.
         */
        @Override
        protected List<Converter<?>> computeValue(Class<?> targetType) {
            // We should not throw here because result of this method is
            // used in canConvertTo(...) to determine supported target types.

            List<Converter<?>> supportsTargetType = new ArrayList<>();
            for (ConverterProvider<?> converterProvider : registeredConverterProviders) {
                Converter<?> converter = converterProvider.get(
                    externalizedProperties,
                    rootConverter
                );
                if (converter.canConvertTo(targetType)) {
                    supportsTargetType.add(converter);
                }
            }
            return supportsTargetType;
        }

        private static List<ConverterProvider<?>> memoizeAll(
                Collection<ConverterProvider<?>> converterProviders
        ) {
            return converterProviders.stream()
                .map(ConverterProvider::memoize)
                .collect(Collectors.toList());
        }
    }

    /**
     * Provider of {@link RootConverter} instance.
     */
    public static interface Provider extends ConverterProvider<RootConverter> {
        /**
         * Get an instance of {@link RootConverter}.
         * 
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @param rootConverter This argument is ignored. No root converter is available 
         * as this method is intended to build the {@link RootConverter} itself. Pass in 
         * {@link NoOpConverter#INSTANCE} here or just use the {@link #get(ExternalizedProperties)}
         * method overload instead.
         * @return An instance of {@link Converter}.
         */
        @Override
        default RootConverter get(
            ExternalizedProperties externalizedProperties, 
            Converter<?> rootConverter
        ) {
            // Ignore rootConverter in provider argument as 
            // we are building the root converter itself here...
            return get(externalizedProperties);
        }

        /**
         * Get an instance of the {@link RootConverter}.
         * 
         * @param externalizedProperties The {@link ExternalizedProperties} instance.
         * @return An instance of {@link RootConverter}.
         */        
        RootConverter get(ExternalizedProperties externalizedProperties);

        /**
         * Create a {@link Provider} which memoizes the result of another
         * {@link Provider}.
         * 
         * @param toMemoize The {@link Provider} whose result will be memoized.
         * @return A {@link Provider} which memoizes the result of another
         * {@link Provider}.
         */
        static Provider memoize(Provider toMemoize) {
            ConverterProvider<RootConverter> memoized = ConverterProvider.memoize(toMemoize);
            
            // Pass in NoOpConverter as rootConverter argument in provider argument as 
            // we are building the root converter itself here...
            return externalizedProperties -> memoized.get(
                externalizedProperties,
                NoOpConverter.INSTANCE
            );
        }
    }
}
