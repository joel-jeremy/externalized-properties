package io.github.joeljeremy7.externalizedproperties.core;

import java.util.Optional;

/**
 * Cache strategy.
 */
public interface CacheStrategy<K, V> {
    /**
     * Cache the value associated to the key.
     * 
     * @param cacheKey The cache key associated to the value.
     * @param value The value to cache.
     */
    void cache(K cacheKey, V value);

    /**
     * Get cached value associated to the cache key.
     * 
     * @param cacheKey The cache key. 
     * @return The cached value if it exists in the cache.
     * Otherwise, an empty {@link Optional} is returned.
     */
    Optional<V> get(K cacheKey);

    /**
     * Expire the cached value associated to the cache key.
     * 
     * @param cacheKey The cache key.
     */
    void expire(K cacheKey);
    
    /**
     * Expire all cache entries.
     */
    void expireAll();
}
