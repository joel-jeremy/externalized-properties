package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Contains ordinal-related functionalities.
 */
public class Ordinals {

    private Ordinals(){}

    /**
     * Create an ordinal resolver.
     * 
     * @param ordinal The ordinal in which this resolver should be placed in the 
     * resolver sequence. The lower the value, the earlier the resolver will be placed
     * in the resolver sequence.
     * @param resolver The decorated resolver.
     * @return The ordinal resolver.
     */
    public static Resolver ordinalResolver(int ordinal, Resolver resolver) {
        return new OrdinalResolver(ordinal, resolver);
    }

    /**
     * Create an ordinal converter.
     * 
     * @param <T> The target type of the decorated converter.
     * @param ordinal The ordinal in which this converter should be placed in the 
     * converter sequence. The lower the value, the earlier the converter will be placed
     * in the converter sequence.
     * @param converter The decorated converter.
     * @return The ordinal converter.
     */
    public static <T> Converter<T> ordinalConverter(int ordinal, Converter<T> converter) {
        return new OrdinalConverter<>(ordinal, converter);
    }

    /**
     * Sort the resolver sequence and unwrap any instances of {@link OrdinalResolver}
     * to the actual decorated resolvers. This follows the sorting rules used by the 
     * {@link #compareOrdinal} method.
     * 
     * @param resolvers The resolvers to sort.
     * @return The ordered resolvers.
     */
    static List<Resolver> sortResolvers(List<Resolver> resolvers) {
        requireNonNull(resolvers, "resolvers");

        return resolvers.stream()
            .sorted(Ordinals::compareOrdinal)
            .map(resolver -> {
                // Discard the OrdinalResolver. The resolver sequence has
                // already been sorted.
                if (resolver instanceof OrdinalResolver) {
                    return ((OrdinalResolver)resolver).unwrap();
                }
                return resolver;
            })
            .collect(Collectors.toList());
    }

    /**
     * Sort the converter sequence and unwrap any instances of {@link OrdinalConverter}
     * to the actual decorated converters. This follows the sorting rules used by the 
     * {@link #compareOrdinal} method.
     * 
     * @param converters The converters to sort.
     * @return The ordered converters.
     */
    static List<Converter<?>> sortConverters(List<Converter<?>> converters) {
        requireNonNull(converters, "converters");

        return converters.stream()
            .sorted(Ordinals::compareOrdinal)
            .map(converter -> {
                // Discard the OrdinalConverter. The converter sequence has
                // already been sorted.
                if (converter instanceof OrdinalConverter<?>) {
                    return ((OrdinalConverter<?>)converter).unwrap();
                }
                return converter;
            })
            .collect(Collectors.toList());
    }

    /**
     * Compare objects based on their ordinals. The lower the ordinal, the earlier the 
     * object will be placed in the sequence.
     * 
     * @param <T> The type of the objects to compare.
     * @param first The first object.
     * @param second the second object.
     * @return The result by comparing the ordinals through the 
     * {@link Integer#compare(int, int)} method.
     */
    static <T> int compareOrdinal(T first, T second) {
        requireNonNull(first, "first");
        requireNonNull(second, "second");

        int firstOrdinal = Integer.MAX_VALUE;
        int secondOrdinal = Integer.MAX_VALUE;

        if (first instanceof Ordinal) {
            firstOrdinal = ((Ordinal)first).ordinal();
        }

        if (second instanceof Ordinal) {
            secondOrdinal = ((Ordinal)second).ordinal();
        }

        return Integer.compare(firstOrdinal, secondOrdinal);
    }

    /**
     * Check whether implements the {@link Ordinal} interface.
     * 
     * @param <T> The type of the object to check.
     * @param obj The object to check.
     * @return {@code true}, if the object implements {@link Ordinal}.
     * Otherwise, {@code false}.
     */
    static <T> boolean isOrdinal(T obj) {
        return obj instanceof Ordinal;
    }

    /**
     * Ordinal.
     */
    static interface Ordinal {
        /**
         * The ordinal in which this object should be placed in an 
         * ordered sequence. The lower the value, the earlier the object will be placed
         * in the sequence.
         * 
         * @return The ordinal in which this object should be placed in an 
         * ordered sequence. The lower the value, the earlier the object will be placed
         * in the sequence.
         */
        int ordinal();
    }
    
    /**
     * Ordinal resolver.
     */
    private static class OrdinalResolver implements Ordinal, Resolver {

        private final Resolver decorated;
        private final int ordinal;
    
        /**
         * Constructor.
         * 
         * @param ordinal The ordinal in which this resolver should be placed in the 
         * resolver sequence. The lower the value, the earlier the resolver will be placed
         * in the resolver sequence.
         * @param decorated The decorated resolver.
         */
        private OrdinalResolver(int ordinal, Resolver decorated) {
            this.ordinal = ordinal;
            this.decorated = requireNonNull(decorated, "decorated");
        }
    
        /** {@inheritDoc}} */
        @Override
        public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
            return decorated.resolve(proxyMethod, propertyName);
        }
    
        /**
         * The ordinal in which this resolver should be placed in the resolver sequence.
         * 
         * @return The ordinal in which this resolver should be placed in the resolver 
         * sequence. The lower the value, the earlier the resolver will be placed
         * in the resolver sequence.
         */
        @Override
        public int ordinal() {
            return ordinal;
        }
    
        /**
         * Unwrap the actual {@link Resolver} instance.
         * 
         * @return Unwrap the actual {@link Resolver} instance.
         */
        private Resolver unwrap() {
            return decorated;
        }
    }

    private static class OrdinalConverter<T> implements Ordinal, Converter<T> {

        private final int ordinal;
        private final Converter<T> decorated;

        /**
         * Constructor.
         * 
         * @param ordinal The ordinal in which this resolver should be placed in the 
         * converter sequence. The lower the value, the earlier the resolver will be placed
         * in the converter sequence.
         * @param decorated The decorated converter.
         */
        public OrdinalConverter(int ordinal, Converter<T> decorated) {
            this.ordinal = ordinal;
            this.decorated = requireNonNull(decorated, "decorated");;
        }

        /** {@inheritDoc}} */
        @Override
        public boolean canConvertTo(Class<?> targetType) {
            return decorated.canConvertTo(targetType);
        }

        /** {@inheritDoc}} */
        @Override
        public ConversionResult<T> convert(
                ProxyMethod proxyMethod, 
                String valueToConvert, 
                Type targetType
        ) {
            return decorated.convert(proxyMethod, valueToConvert, targetType);
        }

        /**
         * The ordinal in which this converter should be placed in the converter sequence.
         * 
         * @return The ordinal in which this converter should be placed in the converter 
         * sequence. The lower the value, the earlier the converter will be placed
         * in the converter sequence.
         */
        @Override
        public int ordinal() {
            return ordinal;
        }

        /**
         * Unwrap the actual {@link Converter} instance.
         * 
         * @return Unwrap the actual {@link Converter} instance.
         */
        private Converter<T> unwrap() {
            return decorated;
        }
    }
}
