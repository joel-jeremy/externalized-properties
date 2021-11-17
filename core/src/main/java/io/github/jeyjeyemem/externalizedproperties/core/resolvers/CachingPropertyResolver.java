package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.DaemonThreadFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNullOrEmptyCollection;
import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNullOrEmptyString;

/**
 * An {@link ExternalizedPropertyResolver} decorator which caches resolved properties
 * for a specified duration.
 */
public class CachingPropertyResolver implements ExternalizedPropertyResolver {
    private final ExternalizedPropertyResolver decorated;
    private final Duration cacheItemLifetime;
    private final CacheStrategy cacheStrategy;
    private final ScheduledExecutorService expiryScheduler =
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(CachingPropertyResolver.class.getName())
        );

    /**
     * Constructor.
     * 
     * @param decorated The decorated resolver where properties will actually be resolved from.
     * @param cacheItemLifetime The duration of cache items in the cache.
     */
    public CachingPropertyResolver(
            ExternalizedPropertyResolver decorated, 
            Duration cacheItemLifetime
    ) {
        this(decorated, cacheItemLifetime, new ConcurrentMapCacheStrategy());
    }

    /**
     * Constructor.
     * 
     * @param decorated The decorated resolver where properties will actually be resolved from.
     * @param cacheItemLifetime The duration of cache items in the cache.
     * @param cacheStrategy The cache strategy.
     */
    public CachingPropertyResolver(
            ExternalizedPropertyResolver decorated,
            Duration cacheItemLifetime,
            CacheStrategy cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /**
     * Resolve property from the decorated {@link ExternalizedPropertyResolver} 
     * and caches the resolved property. If requested property is already in the cache,
     * the cached property will be returned.
     * 
     * @param propertyName The property name.
     * @return The resolved property value. Otherwise, an empty {@link Optional}.
     */
    @Override
    public Optional<ResolvedProperty> resolve(String propertyName) {
        requireNonNullOrEmptyString(propertyName, "propertyName");

        Optional<ResolvedProperty> cached = cacheStrategy.getFromCache(propertyName);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<ResolvedProperty> resolved = decorated.resolve(propertyName);
        if (resolved.isPresent()) {
            // Cache.
            cache(resolved.get());
            return resolved;
        }

        return Optional.empty();
    }

    /**
     * Resolve properties from the decorated {@link ExternalizedPropertyResolver} 
     * and caches the resolved properties. If requested properties are already in the cache,
     * the cached properties will be returned. Any uncached properties will be resolved from 
     * the decorated {@link ExternalizedPropertyResolver}.
     * 
     * @param propertyNames The property names.
     * @return The {@link ExternalizedPropertyResolverResult} which contains the resolved properties
     * and unresolved properties, if there are any.
     */
    @Override
    public ExternalizedPropertyResolverResult resolve(Collection<String> propertyNames) {
        requireNonNullOrEmptyCollection(propertyNames, "propertyNames");

        List<String> nonCachedProperties = new ArrayList<>(propertyNames.size());
        List<ResolvedProperty> resolvedProperties = new ArrayList<>(propertyNames.size());

        for (String propertyName : propertyNames) {
            throwIfNullOrEmptyValue(propertyName);
            Optional<ResolvedProperty> cached = cacheStrategy.getFromCache(propertyName);
            if (cached.isPresent()) {
                resolvedProperties.add(cached.get());
            } else  {
                nonCachedProperties.add(propertyName);
            }
        }

        // Need to resolve remaining non-cached properties.
        if (!nonCachedProperties.isEmpty()) {
            ExternalizedPropertyResolverResult result = decorated.resolve(nonCachedProperties);
            for (ResolvedProperty resolvedProperty : result.resolvedProperties()) {
                // Cache.
                cache(resolvedProperty);
                resolvedProperties.add(resolvedProperty);
            }
        }

        return new ExternalizedPropertyResolverResult(
            propertyNames, 
            resolvedProperties
        );
    }

    private void cache(ResolvedProperty resolvedProperty) {
        // Cache new resolved property.
        cacheStrategy.cache(resolvedProperty);
        // Schedule for expiry.
        scheduleForExpiration(() -> cacheStrategy.expire(resolvedProperty));
    }

    private void scheduleForExpiration(Runnable expiryAction) {
        expiryScheduler.schedule(
            expiryAction, 
            cacheItemLifetime.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }

    private void throwIfNullOrEmptyValue(String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Property name entries must not be null or empty.");
        }
    }

    /**
     * Cache strategy.
     */
    public static interface CacheStrategy {
        /**
         * Cache the resolved property.
         * 
         * @param resolvedProperty The resolved property to cache.
         */
        void cache(ResolvedProperty resolvedProperty);

        /**
         * Resolve property from cache.
         * 
         * @param propertyName The name of the property, 
         * 
         * @return The cached {@link ResolvedProperty} it exists in the cache.
         * Otherwise, an empty {@link Optional} is returned.
         */
        Optional<ResolvedProperty> getFromCache(String propertyName);

        /**
         * Expire the cached resolved property.
         * 
         * @param resolvedProperty The resolved property to expire.
         */
        void expire(ResolvedProperty resolvedProperty);
    }

    /**
     * Default caching strategy which caches resolved properties in a {@link ConcurrentHashMap}.
     */
    public static class ConcurrentMapCacheStrategy implements CacheStrategy {

        private final ConcurrentMap<String, ResolvedProperty> cache;

        /**
         * Default constructor for building a cache strategy that uses an 
         * internal {@link ConcurrentHashMap} cache.
         */
        public ConcurrentMapCacheStrategy() {
            this(new ConcurrentHashMap<>());
        }

        /**
         * Constructor for building a cache strategy that uses an 
         * external {@link ConcurrentMap} cache.
         * 
         * @param cache The cache map.
         */
        public ConcurrentMapCacheStrategy(ConcurrentMap<String, ResolvedProperty> cache) {
            this.cache = requireNonNull(cache, "cache");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void cache(ResolvedProperty resolvedProperty) {
            cache.putIfAbsent(resolvedProperty.name(), resolvedProperty);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<ResolvedProperty> getFromCache(String propertyName) {
            return Optional.ofNullable(cache.get(propertyName));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void expire(ResolvedProperty resolvedProperty) {
            cache.remove(resolvedProperty.name());
        }
    }
}
