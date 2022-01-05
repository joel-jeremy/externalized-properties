package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNullOrEmptyString;

/**
 * An {@link Resolver} decorator which caches resolved properties
 * for a specified duration.
 */
public class CachingResolver implements Resolver {
    private final Resolver decorated;
    private final CacheStrategy<String, String> cacheStrategy;

    /**
     * Constructor.
     * 
     * @param decorated The decorated resolver where properties will actually be resolved from.
     * @param cacheStrategy The cache strategy.
     */
    public CachingResolver(
            Resolver decorated,
            CacheStrategy<String, String> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /**
     * Resolve property from the decorated {@link Resolver} 
     * and caches the resolved property. If requested property is already in the cache,
     * the cached property will be returned.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<String> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        Optional<String> cached = cacheStrategy.get(propertyName);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<String> resolved = decorated.resolve(propertyName);
        if (resolved.isPresent()) {
            // Cache.
            cacheStrategy.cache(propertyName, resolved.get());
            return resolved;
        }

        return Optional.empty();
    }

    /**
     * Resolve properties from the decorated {@link Resolver} 
     * and caches the resolved properties. If requested properties are already in the cache,
     * the cached properties will be returned. Any uncached properties will be resolved from 
     * the decorated {@link Resolver}.
     * 
     * @param propertyNames The property names.
     * @return The {@link ResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        List<String> nonCachedProperties = new ArrayList<>(propertyNames.size());
        ResolverResult.Builder resultBuilder = ResolverResult.builder(propertyNames);

        for (String propertyName : propertyNames) {
            throwIfNullOrEmptyValue(propertyName);
            Optional<String> cachedValue = cacheStrategy.get(propertyName);
            if (cachedValue.isPresent()) {
                resultBuilder.add(propertyName, cachedValue.get());
            } else  {
                nonCachedProperties.add(propertyName);
            }
        }

        // Need to resolve remaining non-cached properties.
        if (!nonCachedProperties.isEmpty()) {
            ResolverResult result = decorated.resolve(nonCachedProperties);
            for (Map.Entry<String, String> resolvedProperty : result.resolvedProperties().entrySet()) {
                // Cache.
                cacheStrategy.cache(resolvedProperty.getKey(), resolvedProperty.getValue());
                resultBuilder.add(
                    resolvedProperty.getKey(), 
                    resolvedProperty.getValue()
                );
            }
        }

        return resultBuilder.build();
    }

    private void throwIfNullOrEmptyValue(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name entries must not be null or empty.");
        }
    }
}
