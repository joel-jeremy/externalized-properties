package io.github.joeljeremy7.externalizedproperties.core;

import java.util.Optional;

/**
 * The mechanism to resolve properties from various external sources.
 */
public interface Resolver {
    /**
     * Resolve property from a configured source.
     * 
     * @param context The proxy method invocation context.
     * @param propertyName The name of the property to resolve.
     * @return The property value, if successfully resolved. Otherwise, an empty 
     * {@link Optional}.
     */
    Optional<String> resolve(InvocationContext context, String propertyName);
}
