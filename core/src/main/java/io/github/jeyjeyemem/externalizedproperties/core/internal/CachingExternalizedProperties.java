package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;

import java.lang.reflect.Type;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * {@link ExternalizedProperties} decorator to enable property resolution caching.
 */
public class CachingExternalizedProperties implements ExternalizedProperties {

    private final ExternalizedProperties decorated;
    private final CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy;
    private final CacheStrategy<String, String> variableExpansionCacheStrategy;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link ExternalizedProperties} instance.
     * @param resolvedPropertyCacheStrategy The resolved property cache strategy
     * keyed by property name and whose values are the resolved property values. 
     * @param variableExpansionCacheStrategy The variable expansion cache strategy
     * keyed by source/unexpanded string and whose values are the expanded string. 
     */
    public CachingExternalizedProperties(
            ExternalizedProperties decorated,
            CacheStrategy<String, Optional<?>> resolvedPropertyCacheStrategy,
            CacheStrategy<String, String> variableExpansionCacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.resolvedPropertyCacheStrategy = requireNonNull(
            resolvedPropertyCacheStrategy, 
            "resolvedPropertyCacheStrategy"
        );
        this.variableExpansionCacheStrategy = requireNonNull(
            variableExpansionCacheStrategy, 
            "variableExpansionCacheStrategy"
        );
    }

    /** {@inheritDoc} */
    @Override
    public <T> T proxy(Class<T> proxyInterface) {
        return decorated.proxy(proxyInterface);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader) {
        return decorated.proxy(proxyInterface, classLoader);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolveProperty(String propertyName) {
        Optional<Optional<?>> cached = resolvedPropertyCacheStrategy.get(propertyName);
        if (cached.isPresent()) {
            @SuppressWarnings("unchecked")
            Optional<String> result = (Optional<String>)cached.get();
            return result;
        }

        Optional<String> resolved = decorated.resolveProperty(propertyName);
        return cacheResolvedValue(propertyName, resolved);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(String propertyName, Class<T> expectedType) {
        return (Optional<T>)resolveProperty(propertyName, (Type)expectedType);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName, 
            TypeReference<T> expectedType
    ) {
        return (Optional<T>)resolveProperty(
            propertyName, 
            requireNonNull(expectedType, "expectedType").type()
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> resolveProperty(
            String propertyName, 
            Type expectedType
    ) {
        Optional<Optional<?>> cached = resolvedPropertyCacheStrategy.get(propertyName);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Property name variable already expanded.
        Optional<?> resolved = decorated.resolveProperty(
            propertyName, 
            expectedType
        );

        return cacheResolvedValue(propertyName, resolved);
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String source) {
        Optional<String> cached = variableExpansionCacheStrategy.get(source);
        if (cached.isPresent()) {
            return cached.get();
        }

        String expanded = decorated.expandVariables(source);
        return cacheExpandedValue(source, expanded);
    }

    private String cacheExpandedValue(String source, String expanded) {
        variableExpansionCacheStrategy.cache(source, expanded);
        return expanded;
    }

    private <T> Optional<T> cacheResolvedValue(String propertyName, Optional<T> resolved) {
        resolvedPropertyCacheStrategy.cache(propertyName, resolved);
        return resolved;
    }
}
