package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A stub {@link Resolver} implemenation that resolves properties based on 
 * a provided value resolver function. 
 * 
 * <p>By default, it will resolve all properties by 
 * returning the property name suffixed with "-value".</p>
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

    public StubResolver(Function<String, String> valueResolver) {
        this.valueResolver = valueResolver;
    }

    @Override
    public Optional<String> resolve(InvocationContext context, String propertyName) {
        String value = valueResolver.apply(propertyName);
        if (value != null) {
            // Add for tracking.
            trackedResolvedProperties.put(propertyName, value);
            
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public Function<String, String> valueResolver() {
        return valueResolver;
    }

    public Map<String, String> resolvedProperties() {
        return Collections.unmodifiableMap(trackedResolvedProperties);
    }

    public Set<String> resolvedPropertyNames() {
        return Collections.unmodifiableSet(trackedResolvedProperties.keySet());
    }
}
