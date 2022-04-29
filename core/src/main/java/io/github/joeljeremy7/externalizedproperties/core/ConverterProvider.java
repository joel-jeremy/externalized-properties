package io.github.joeljeremy7.externalizedproperties.core;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Provider of {@link Converter} instance.
 * @param <T> The type of converter.
 */
public interface ConverterProvider<T extends Converter<?>> {
    /**
     * Get an instance of {@link Converter}.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @param rootConverter The root {@link Converter} instance. The root converter
     * which contains all registered converters and delegates accordingly. 
     * If converter implementation requires conversion of values to other types,
     * it may do so by requesting conversion via the root converter. 
     * @return An instance of {@link Converter}.
     */
    T get(
        ExternalizedProperties externalizedProperties,
        Converter<?> rootConverter
    );

    /**
     * Create a {@link ConverterProvider} which always returns the provided 
     * {@link Converter} instance.
     * 
     * @param <T> The type of converter.
     * @param converter The {@link Converter} instance to be returned by the resulting
     * {@link ConverterProvider}.
     * @return A {@link ConverterProvider} which always returns the provided
     * {@link Converter} instance.
     */
    static <T extends Converter<?>> ConverterProvider<T> of(T converter) {
        requireNonNull(converter, "converter");
        return (externalizedProperties, rootConverter) -> converter;
    }

    /**
     * Create a {@link ConverterProvider} which memoizes the result of another
     * {@link ConverterProvider}.
     * 
     * @param <T> The type of converter.
     * @param provider The {@link ConverterProvider} whose result will be memoized.
     * @return A {@link ConverterProvider} which memoizes the result of another
     * {@link ConverterProvider}.
     */
    static <T extends Converter<?>> ConverterProvider<T> memoize(
            ConverterProvider<T> provider
    ) {
        if (provider instanceof Memoized) {
            return provider;
        }
        return new Memoized<>(provider);
    }

    /**
     * A {@link ConverterProvider} which memoizes the result of another
     * {@link ConverterProvider}.
     */
    static final class Memoized<T extends Converter<?>> implements ConverterProvider<T> {

        private final ConverterProvider<T> provider;
        private final AtomicReference<T> memoized = new AtomicReference<>(null);

        private Memoized(ConverterProvider<T> provider) {
            this.provider = requireNonNull(provider, "provider");
        }

        /** {@inheritDoc} */
        @Override
        public T get(
                ExternalizedProperties externalizedProperties,
                Converter<?> rootConverter
        ) {
            T result = memoized.get();
            if (result == null) {
                result = provider.get(externalizedProperties, rootConverter);
                memoized.compareAndSet(null, result);
                result = memoized.get();
                if (result == null) {
                    throw new IllegalStateException("Memoized provider returned null.");
                }
            }
            return result;
        }
    }
}