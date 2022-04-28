package io.github.joeljeremy7.externalizedproperties.core;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Provider of {@link Processor} instance.
 * @param <T> The type of processor.
 */
public interface ProcessorProvider<T extends Processor> {
    /**
     * Get an instance of {@link Processor}.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return An instance of {@link Processor}.
     */
    T get(ExternalizedProperties externalizedProperties);

    /**
     * Create a {@link ProcessorProvider} which always returns the provided 
     * {@link Processor} instance.
     * 
     * @param <T> The type of processor.
     * @param processor The {@link Processor} instance to be returned by the resulting
     * {@link ProcessorProvider}.
     * @return A {@link ProcessorProvider} which always returns the provided 
     * {@link Processor} instance.
     */
    static <T extends Processor> ProcessorProvider<T> of(T processor) {
        requireNonNull(processor, "processor");
        return externalizedProperties -> processor;
    }

    /**
     * Create a {@link ProcessorProvider} which memoizes the result of another
     * {@link ProcessorProvider}.
     * 
     * @param <T> The type of processor.
     * @param provider The {@link ProcessorProvider} whose result will be memoized.
     * @return A {@link ProcessorProvider} which memoizes the result of another
     * {@link ProcessorProvider}.
     */
    static <T extends Processor> ProcessorProvider<T> memoize(
            ProcessorProvider<T> provider
    ) {
        requireNonNull(provider, "provider");

        final AtomicReference<T> memoized = new AtomicReference<>(null);
        return externalizedProperties -> {
            T result = memoized.get();
            if (result == null) {
                result = provider.get(externalizedProperties);
                memoized.compareAndSet(null, result);
                result = memoized.get();
                if (result == null) {
                    throw new IllegalStateException("Memoized provider returned null.");
                }
            }
            return result;
        };
    }
}