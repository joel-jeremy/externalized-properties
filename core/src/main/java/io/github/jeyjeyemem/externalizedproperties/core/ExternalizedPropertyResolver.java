package io.github.jeyjeyemem.externalizedproperties.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The mechanism to resolve properties from various external sources.
 */
public interface ExternalizedPropertyResolver {
    /**
     * Resolve the properties from an external source.
     * 
     * @param propertyNames The collection of property names.
     * @return The result object containing the resolved and unresolved properties.
     */
    ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames);

    /**
     * Resolve the properties from an external source.
     * 
     * @param propertyNames The array of property names.
     * @return The result object containing the resolved and unresolved properties.
     */
    default ExternalizedPropertyResolverResult resolve(String... propertyNames) {
        return resolve(propertyNames == null ? 
            Collections.emptyList() : 
            Arrays.asList(propertyNames));
    }
}
