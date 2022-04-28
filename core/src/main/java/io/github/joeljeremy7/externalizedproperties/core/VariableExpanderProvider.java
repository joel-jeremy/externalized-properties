package io.github.joeljeremy7.externalizedproperties.core;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Provider of {@link VariableExpander} instance.
 */
public interface VariableExpanderProvider<T extends VariableExpander> {
    /**
     * Get an instance of {@link VariableExpander}.
     * 
     * @param externalizedProperties The {@link ExternalizedProperties} instance.
     * @return An instance of {@link VariableExpander}.
     */
    T get(ExternalizedProperties externalizedProperties);

    /**
     * Create a {@link VariableExpanderProvider} which always returns the provided 
     * {@link VariableExpander} instance.
     * 
     * @param <T> The type of variable expander.
     * @param variableExpander The {@link VariableExpander} instance to be returned by the 
     * resulting {@link VariableExpanderProvider}.
     * @return A {@link VariableExpanderProvider} which always returns the provided 
     * {@link VariableExpander} instance.
     */
    static <T extends VariableExpander> VariableExpanderProvider<T> of(T variableExpander) {
        requireNonNull(variableExpander, "variableExpander");
        return externalizedProperties -> variableExpander;
    }

    /**
     * Create a {@link VariableExpanderProvider} which memoizes the result of another
     * {@link VariableExpanderProvider}.
     * 
     * @param <T> The type of variable expander.
     * @param provider The {@link VariableExpanderProvider} whose result will be memoized.
     * @return A {@link VariableExpanderProvider} which memoizes the result of another
     * {@link VariableExpanderProvider}.
     */
    static <T extends VariableExpander> VariableExpanderProvider<T> memoize(
            VariableExpanderProvider<T> provider
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