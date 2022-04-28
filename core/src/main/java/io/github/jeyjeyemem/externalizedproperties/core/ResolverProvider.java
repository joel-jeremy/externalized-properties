package io.github.jeyjeyemem.externalizedproperties.core;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Provider of {@link Resolver} instance.
 * @param <T> The type of resolver.
 */
public interface ResolverProvider<T extends Resolver> {
    /**
     * Get an instance of {@link Resolver}.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return An instance of {@link Resolver}.
     */
    T get(ExternalizedProperties externalizedProperties);

    /**
     * Create a {@link ResolverProvider} which always returns the provided 
     * {@link Resolver} instance.
     * 
     * @param <T> The type of resolver.
     * @param resolver The {@link Resolver} instance to be returned by the resulting
     * {@link ResolverProvider}.
     * @return A {@link ResolverProvider} which always returns the provided 
     * {@link Resolver} instance.
     */
    static <T extends Resolver> ResolverProvider<T> of(T resolver) {
        requireNonNull(resolver, "resolver");
        return externalizedProperties -> resolver;
    }

    /**
     * Create a {@link ResolverProvider} which memoizes the result of another
     * {@link ResolverProvider}.
     * 
     * @param <T> The type of resolver.
     * @param provider The {@link ResolverProvider} whose result will be memoized.
     * @return A {@link ResolverProvider} which memoizes the result of another
     * {@link ResolverProvider}.
     */
    static <T extends Resolver> ResolverProvider<T> memoize(
            ResolverProvider<T> provider
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