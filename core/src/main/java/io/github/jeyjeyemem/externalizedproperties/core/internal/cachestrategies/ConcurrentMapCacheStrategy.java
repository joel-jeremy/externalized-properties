package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Caching strategy which uses a {@link ConcurrentHashMap} as cache.
 */
public class ConcurrentMapCacheStrategy<K, V> implements CacheStrategy<K, V> {

    private final ConcurrentMap<K, V> cache;

    /**
     * Default constructor for building a cache strategy that uses an 
     * internal {@link ConcurrentHashMap} cache.
     */
    public ConcurrentMapCacheStrategy() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Package-private constructor for building a cache strategy that uses an 
     * external {@link ConcurrentMap} cache.
     * 
     * @param cache The cache map.
     */
    ConcurrentMapCacheStrategy(ConcurrentMap<K, V> cache) {
        this.cache = requireNonNull(cache, "cache");
    }

    /** {@inheritDoc} */
    @Override
    public void cache(K cacheKey, V value) {
        cache.putIfAbsent(cacheKey, value);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<V> get(K cacheKey) {
        return Optional.ofNullable(cache.get(cacheKey));
    }

    /** {@inheritDoc} */
    @Override
    public void expire(K cacheKey) {
        cache.remove(cacheKey);
    }

    /** {@inheritDoc} */
    @Override
    public void expireAll() {
        cache.clear();
    }
}