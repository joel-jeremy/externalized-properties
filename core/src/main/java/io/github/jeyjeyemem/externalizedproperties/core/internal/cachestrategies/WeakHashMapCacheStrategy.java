package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;

import java.util.Optional;
import java.util.WeakHashMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Caching strategy which uses a {@link WeakHashMap} as cache.
 */
public class WeakHashMapCacheStrategy<K, V> implements CacheStrategy<K, V> {

    private final WeakHashMap<K, V> weakCache;

    /**
     * Default constructor for building a cache strategy that uses an 
     * internal {@link WeakHashMap} cache.
     */
    public WeakHashMapCacheStrategy() {
        this(new WeakHashMap<>());
    }

    /**
     * Package-private constructor for building a cache strategy that uses an 
     * external {@link WeakHashMap} cache.
     * 
     * @param weakCache The weak cache map.
     */
    WeakHashMapCacheStrategy(WeakHashMap<K, V> weakCache) {
        this.weakCache = requireNonNull(weakCache, "weakCache");
    }

    /** {@inheritDoc} */
    @Override
    public void cache(K cacheKey, V value) {
        weakCache.putIfAbsent(cacheKey, value);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<V> get(K cacheKey) {
        return Optional.ofNullable(weakCache.get(cacheKey));
    }

    /** {@inheritDoc} */
    @Override
    public void expire(K cacheKey) {
        weakCache.remove(cacheKey);
    }

    /** {@inheritDoc} */
    @Override
    public void expireAll() {
        weakCache.clear();
    }
}