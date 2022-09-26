package io.github.joeljeremy7.externalizedproperties.core.testfixtures;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** A stub {@link CacheStrategy} implementation. */
public class StubCacheStrategy<K, V> implements CacheStrategy<K, V> {
  private final Map<K, V> cache = new HashMap<>();
  private final Map<K, CountDownLatch> expiryLatchByCacheKey = new HashMap<>();

  /** {@inheritDoc} */
  @Override
  public void cache(K cacheKey, V value) {
    cache.putIfAbsent(cacheKey, value);
    // Add latch for newly added items.
    expiryLatchByCacheKey.putIfAbsent(cacheKey, new CountDownLatch(1));
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
    // Release latch if anything is waiting for it.
    CountDownLatch expiryLatch = expiryLatchByCacheKey.get(cacheKey);
    if (expiryLatch != null) {
      expiryLatch.countDown();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void expireAll() {
    cache.clear();
    expiryLatchByCacheKey.forEach((k, expiryLatch) -> expiryLatch.countDown());
  }

  /**
   * Get underlying cache map.
   *
   * @return The underlying cache map.
   */
  public Map<K, V> getCache() {
    return Collections.unmodifiableMap(cache);
  }

  /**
   * Wait for the key to be expired.
   *
   * @param cacheKey The cache key to wait for.
   * @return {@code true}, if the cache key has been expired. Otherwise, {@code false} if the
   *     waiting time has elapsed before expiry.
   * @throws InterruptedException if the current thread is interrupted while waiting.
   */
  public boolean waitForExpiry(K cacheKey) throws InterruptedException {
    CountDownLatch expiryLatch = expiryLatchByCacheKey.get(cacheKey);
    if (expiryLatch == null) {
      throw new IllegalArgumentException("Expiry latch for cache key not found: " + cacheKey);
    }
    return expiryLatch.await(5, TimeUnit.SECONDS);
  }

  /**
   * Wait for keys to be expired.
   *
   * @param cacheKeys The cache keys to wait for.
   * @return {@code true}, if the cache keys have been expired. Otherwise, {@code false} if the
   *     waiting time has elapsed before expiry.
   * @throws InterruptedException if the current thread is interrupted while waiting.
   */
  public boolean waitForExpiry(Collection<K> cacheKeys) throws InterruptedException {
    for (K cacheKey : cacheKeys) {
      if (!waitForExpiry(cacheKey)) {
        // Timeout elapsed.
        return false;
      }
    }
    return true;
  }
}
