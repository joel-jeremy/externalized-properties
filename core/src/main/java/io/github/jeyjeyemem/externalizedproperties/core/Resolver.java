package io.github.jeyjeyemem.externalizedproperties.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * The mechanism to resolve properties from various external sources.
 */
public interface Resolver {
    /**
     * Resolve property from an external source.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    Optional<String> resolve(String propertyName);

    /**
     * Resolve the properties from an external source.
     * 
     * @param propertyNames The collection of property names.
     * @return The result object containing the resolved and unresolved properties.
     */
    ResolverResult resolve(Collection<String> propertyNames);

    /**
     * Resolve the properties from an external source.
     * 
     * @param propertyNames The array of property names.
     * @return The result object containing the resolved and unresolved properties.
     */
    default ResolverResult resolve(String... propertyNames) {
        return resolve(propertyNames == null ? 
            Collections.emptyList() : 
            Arrays.asList(propertyNames));
    }
}
