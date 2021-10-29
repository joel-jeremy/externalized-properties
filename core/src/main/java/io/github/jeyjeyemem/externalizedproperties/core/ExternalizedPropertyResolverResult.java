package io.github.jeyjeyemem.externalizedproperties.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The result object containing the resolved and unresolved properties.
 */
public class ExternalizedPropertyResolverResult {

    private final Map<String, ResolvedProperty> resolvedPropertiesByName;
    private final Set<String> unresolvedPropertyNames;

    /**
     * Constructor.
     * 
     * @param propertiesToResolve Names of properties that was requested to be resolved.
     * @param resolvedProperties Successfully resolved properties.
     */
    public ExternalizedPropertyResolverResult(
            Collection<String> propertiesToResolve,
            Collection<ResolvedProperty> resolvedProperties
    ) {
        requireNonNull(propertiesToResolve, "propertiesToResolve");
        requireNonNull(resolvedProperties, "resolvedProperties");

        this.resolvedPropertiesByName = resolvedProperties.stream()
            .collect(Collectors.toMap(ResolvedProperty::name, rp -> rp));
        
        this.unresolvedPropertyNames = getUnresolvedPropertyNames(
            propertiesToResolve, 
            resolvedProperties
        );
    }

    /**
     * Returns the collection of resolved properties.
     * 
     * @return The collection of resolved properties.
     */
    public Collection<ResolvedProperty> resolvedProperties() {
        return Collections.unmodifiableCollection(resolvedPropertiesByName.values());
    }

    /**
     * Returns the collection of unresolved properties.
     * 
     * @return The collection of unresolved properties.
     */
    public Set<String> unresolvedPropertyNames() {
        return Collections.unmodifiableSet(unresolvedPropertyNames);
    }

    /**
     * Check if there are any resolved properties.
     * 
     * @return {@code true}, if there are any resolved properties. Otherwise, {@code false}.
     */
    public boolean hasResolvedProperties() {
        return !resolvedPropertiesByName.isEmpty();
    }

    /**
     * Check if there are any unresolved properties.
     * 
     * @return {@code true}, if there are any unresolved properties. Otherwise, {@code false}.
     */
    public boolean hasUnresolvedProperties() {
        return !unresolvedPropertyNames.isEmpty();
    }

    /**
     * Find resolved property with the given name.
     * 
     * @param name The name of the property.
     * @return An optional {@link ResolvedProperty} instance if it exists. Otherwise, false.
     */
    public Optional<ResolvedProperty> findResolvedProperty(String name) {
        return Optional.ofNullable(resolvedPropertiesByName.get(name));
    }

    private static Set<String> getUnresolvedPropertyNames(
            Collection<String> propertiesToResolve,
            Collection<ResolvedProperty> resolvedProperties) 
    {
        return propertiesToResolve.stream()
            .filter(pn -> resolvedProperties.stream().noneMatch(rp -> rp.name().equals(pn)))
            .collect(Collectors.toSet());
    }
}