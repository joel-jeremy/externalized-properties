package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StubExternalizedPropertyResolver implements ExternalizedPropertyResolver {

    private final Set<String> resolvedPropertyNames = new HashSet<>();
    private final Function<String, String> valueResolver;

    public StubExternalizedPropertyResolver() {
        // Return property name as value.
        this(propertyName -> propertyName);
    }

    public StubExternalizedPropertyResolver(
            Function<String, String> valueResolver
    ) {
        this.valueResolver = valueResolver;
    }

    @Override
    public Optional<ResolvedProperty> resolve(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("propertyName must not be null or empty.");
        }

        String value = valueResolver.apply(propertyName);
        if (value != null) {
            // Add for tracking.
            resolvedPropertyNames.add(propertyName);
            
            return Optional.of(
                ResolvedProperty.with(propertyName, value)
            );
        }
        return Optional.empty();
    }

    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalArgumentException("propertyNames must not be null or empty.");
        }

        List<ResolvedProperty> resolved = propertyNames.stream()
            .map(pn -> {
                String value = valueResolver.apply(pn);
                if (value != null) {
                    return ResolvedProperty.with(pn, value);
                }
                
                return null;
            })
            .filter(Objects::nonNull) // Discard nulls.
            .collect(Collectors.toList());

        // Add for tracking.
        resolved.forEach(r -> resolvedPropertyNames.add(r.name()));

        return new ExternalizedPropertyResolverResult(
            propertyNames, 
            resolved
        );
    }

    public Function<String, String> valueResolver() {
        return valueResolver;
    }

    public Set<String> resolvedPropertyNames() {
        return java.util.Collections.unmodifiableSet(resolvedPropertyNames);
    }
}
