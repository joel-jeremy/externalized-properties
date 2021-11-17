package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * {@code ExternalizedProperties} decorator to enable property resolution caching.
 */
public class CachingExternalizedProperties implements ExternalizedProperties {

    private final ExternalizedProperties decorated;
    private final Map<String, Object> cache;
    private final Duration cacheItemLifetime;
    private final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(CachingExternalizedProperties.class.getName())
        );

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link ExternalizedProperties} instance.
     * @param cacheItemLifetime The duration of cache items in the cache.
     */
    public CachingExternalizedProperties(
            ExternalizedProperties decorated,
            Duration cacheItemLifetime
    ) {
        this(decorated, new HashMap<>(), cacheItemLifetime);
    }

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link ExternalizedProperties} instance.
     * @param cache The cache map.
     * @param cacheItemLifetime The duration of cache items in the cache.
     */
    public CachingExternalizedProperties(
            ExternalizedProperties decorated,
            Map<String, Object> cache,
            Duration cacheItemLifetime
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cache = requireNonNull(cache, "cache");
        this.cacheItemLifetime = requireNonNull(
            cacheItemLifetime,
            "cacheItemLifetime"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T proxy(Class<T> proxyInterface) {
        return decorated.proxy(proxyInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader) {
        return decorated.proxy(proxyInterface, classLoader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> resolveProperty(String propertyName) {
        Object cached = cache.get(propertyName);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            Optional<String> result = (Optional<String>)cached;
            return result;
        }

        Optional<String> resolved = decorated.resolveProperty(propertyName);
        return cache(propertyName, resolved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> resolveProperty(String propertyName, Class<T> expectedType) {
        return resolveProperty(propertyName, (Type)expectedType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> resolveProperty(
            String propertyName, 
            TypeReference<T> expectedType
    ) {
        return resolveProperty(propertyName, expectedType.type());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> resolveProperty(
            String propertyName, 
            Type expectedType
    ) {
        Object cached = cache.get(propertyName);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            Optional<T> result = (Optional<T>)cached;
            return result;
        }

        // Property name variable already expanded.
        Optional<T> resolved = decorated.resolveProperty(
            propertyName, 
            expectedType
        );

        return cache(propertyName, resolved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String expandVariables(String source) {
        return decorated.expandVariables(source);
    }

    private <T> Optional<T> cache(String propertyName, Optional<T> resolved) {
        cache.putIfAbsent(propertyName, resolved);
        scheduleForExpiry(() -> cache.remove(propertyName));
        return resolved;
    }

    private void scheduleForExpiry(Runnable expiryAction) {
        expiryScheduler.schedule(
            expiryAction, 
            cacheItemLifetime.toMillis(), 
            TimeUnit.MILLISECONDS
        );
    }
}
