package io.github.jeyjeyemem.externalizedproperties.core.testentities;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A stub {@link Resolver} implemenation that resolves properties based on 
 * a provider value resolver function. By default, it will defaul any property by returning the 
 * property name suffixed with "-value".
 */
public class StubResolver implements Resolver {

    public static final String DEFAULT_PROPERTY_NAME_SUFFIX = "-value";

    // Return property name as value suffixed with "-value".
    public static final Function<String, String> DEFAULT_VALUE_RESOLVER = 
        propertyName -> propertyName + DEFAULT_PROPERTY_NAME_SUFFIX;

    public static final Function<String, String> NULL_VALUE_RESOLVER = 
        propertyName -> null;

    private final Map<String, String> trackedResolvedProperties = new HashMap<>();
    private final Function<String, String> valueResolver;

    public StubResolver() {
        this(DEFAULT_VALUE_RESOLVER);
    }

    public StubResolver(
            Function<String, String> valueResolver
    ) {
        this.valueResolver = valueResolver;
    }

    @Override
    public Optional<String> resolve(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("propertyName must not be null or empty.");
        }

        String value = valueResolver.apply(propertyName);
        if (value != null) {
            // Add for tracking.
            trackedResolvedProperties.put(propertyName, value);
            
            return Optional.of(value);
        }
        return Optional.empty();
    }

    @Override
    public Result resolve(Collection<String> propertyNames) {
        if (propertyNames == null || propertyNames.isEmpty()) {
            throw new IllegalArgumentException("propertyNames must not be null or empty.");
        }
        Result.Builder resultBuilder = Result.builder(propertyNames);

        for (String propertyName : propertyNames) {
            String resolvedValue = valueResolver.apply(propertyName);
            if (resolvedValue != null) {
                resultBuilder.add(propertyName, resolvedValue);
            }
        }

        Result result = resultBuilder.build();

        // Add for tracking.
        trackedResolvedProperties.putAll(result.resolvedProperties());

        return result;
    }

    public Map<String, String> resolvedProperties() {
        return Collections.unmodifiableMap(trackedResolvedProperties);
    }

    public Set<String> resolvedPropertyNames() {
        return Collections.unmodifiableSet(trackedResolvedProperties.keySet());
    }
}
