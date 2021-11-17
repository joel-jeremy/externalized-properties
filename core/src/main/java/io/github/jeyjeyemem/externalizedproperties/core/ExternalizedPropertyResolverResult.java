package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.UnresolvedPropertyException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

        this.resolvedPropertiesByName = new HashMap<>(resolvedProperties.size());
        for (ResolvedProperty resolvedProperty : resolvedProperties) {
            resolvedPropertiesByName.put(resolvedProperty.name(), resolvedProperty);
        }

        this.unresolvedPropertyNames = getUnresolvedPropertyNames(
            propertiesToResolve, 
            resolvedPropertiesByName.keySet()
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
     * @return An optional {@link ResolvedProperty} instance if it exists. 
     * Otherwise, an empty optional instance.
     */
    public Optional<ResolvedProperty> findResolvedProperty(String name) {
        return Optional.ofNullable(resolvedPropertiesByName.get(name));
    }

    /**
     * Find resolved property with the given name.
     * 
     * @param name The name of the property.
     * @return An optional containing the property value if it exists. 
     * Otherwise, an empty optional instance.
     */
    public Optional<String> findResolvedPropertyValue(String name) {
        return findResolvedProperty(name).map(ResolvedProperty::value);
    }

    /**
     * Find resolved property with the given name or else throw an exception 
     * if the property has not been resolved.
     * 
     * @param name The name of the property.
     * @return The required property value.
     */
    public String findRequiredPropertyValue(String name) {
        ResolvedProperty resolvedProperty = resolvedPropertiesByName.get(name);
        if (resolvedProperty == null) {
            throw new UnresolvedPropertyException(
                name,
                "Requested property named " + name + " has not been resolved."
            );
        }
        return resolvedProperty.value();
    }

    private static Set<String> getUnresolvedPropertyNames(
            Collection<String> propertiesToResolve,
            Set<String> resolvedPropertyNames) 
    {
        Set<String> unresolvedPropertyNames = new HashSet<>(
            propertiesToResolve.size() - resolvedPropertyNames.size()
        );

        for (String propertyName : propertiesToResolve) {
            if (!resolvedPropertyNames.contains(propertyName)) {
                unresolvedPropertyNames.add(propertyName);
            }
        }

        return unresolvedPropertyNames;
    }
}