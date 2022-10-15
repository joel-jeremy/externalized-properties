package io.github.joeljeremy.externalizedproperties.core.internal.caching;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Caching strategy which uses a {@link ConcurrentHashMap} as cache. */
public class ConcurrentHashMapCacheStrategy<K, V> implements CacheStrategy<K, V> {

  private final ConcurrentMap<K, V> cache;

  /**
   * Default constructor for building a cache strategy that uses an internal {@link
   * ConcurrentHashMap} cache.
   */
  public ConcurrentHashMapCacheStrategy() {
    this(new ConcurrentHashMap<>());
  }

  /**
   * Package-private constructor for building a cache strategy that uses an external {@link
   * ConcurrentMap} cache.
   *
   * @param cache The cache map.
   */
  ConcurrentHashMapCacheStrategy(ConcurrentMap<K, V> cache) {
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
