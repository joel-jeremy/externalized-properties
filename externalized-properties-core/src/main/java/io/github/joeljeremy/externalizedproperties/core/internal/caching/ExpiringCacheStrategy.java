package io.github.joeljeremy.externalizedproperties.core.internal.caching;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.internal.DaemonThreadFactory;
import io.github.joeljeremy.externalizedproperties.core.internal.Internal;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link CacheStrategy} decorator that automatically expires cached items after the given cache
 * item lifetime duration. This expires individual cache items instead of the whole cache and the
 * expiry timer for a cache item starts on {@link ExpiringCacheStrategy#cache(Object, Object)}
 * method invocation.
 */
@Internal
public class ExpiringCacheStrategy<K, V> implements CacheStrategy<K, V> {

  private final ScheduledExecutorService expiryScheduler =
      Executors.newSingleThreadScheduledExecutor(
          new DaemonThreadFactory(ExpiringCacheStrategy.class.getName()));
  private final CacheStrategy<K, V> decorated;
  private final Duration cacheItemLifetime;

  /**
   * Constructor.
   *
   * @param decorated The decorated {@link CacheStrategy}.
   * @param cacheItemLifetime The allowed duration of cache items in the cache.
   */
  public ExpiringCacheStrategy(CacheStrategy<K, V> decorated, Duration cacheItemLifetime) {
    this.decorated = requireNonNull(decorated, "decorated");
    this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
  }

  /**
   * Cache the value associated to the key and schedule individual keys for expiry based on the
   * configured cache item lifetime.
   *
   * @param cacheKey The cache key associated to the value.
   * @param cacheValue The value to cache.
   */
  @Override
  public void cache(K cacheKey, V cacheValue) {
    decorated.cache(cacheKey, cacheValue);
    scheduleForExpiry(cacheKey);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<V> get(K cacheKey) {
    return decorated.get(cacheKey);
  }

  /** {@inheritDoc} */
  @Override
  public void expire(K cacheKey) {
    decorated.expire(cacheKey);
  }

  /** {@inheritDoc} */
  @Override
  public void expireAll() {
    decorated.expireAll();
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  private void scheduleForExpiry(K cacheKey) {
    expiryScheduler.schedule(
        () -> expire(cacheKey), cacheItemLifetime.toMillis(), TimeUnit.MILLISECONDS);
  }
}
