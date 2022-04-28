package io.github.jeyjeyemem.externalizedproperties.core.testfixtures;

import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;

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

    public static ResolverProvider<StubResolver> provider() {
        return ep -> new StubResolver();
    }

    public static ResolverProvider<StubResolver> provider(
            Function<String, String> valueResolver
    ) {
        return ep -> new StubResolver(valueResolver);
    }

    @Override
    public Optional<String> resolve(ProxyMethod proxyMethod, String propertyName) {
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

    // @Override
    // public ResolverResult resolve(ProxyMethod proxyMethod, Collection<String> propertyNames) {
    //     if (propertyNames == null || propertyNames.isEmpty()) {
    //         throw new IllegalArgumentException("propertyNames must not be null or empty.");
    //     }
    //     ResolverResult result = ResolverResult.builder(propertyNames)
    //         .map(valueResolver)
    //         .build();

    //     // Add for tracking.
    //     trackedResolvedProperties.putAll(result.resolvedProperties());

    //     return result;
    // }

    public Map<String, String> resolvedProperties() {
        return Collections.unmodifiableMap(trackedResolvedProperties);
    }

    public Set<String> resolvedPropertyNames() {
        return Collections.unmodifiableSet(trackedResolvedProperties.keySet());
    }
}
