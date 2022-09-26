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

  @Override
  public void cache(K cacheKey, V value) {
    cache.putIfAbsent(cacheKey, value);
    // Add latch for newly added items.
    expiryLatchByCacheKey.putIfAbsent(cacheKey, new CountDownLatch(1));
  }

  @Override
  public Optional<V> get(K cacheKey) {
    return Optional.ofNullable(cache.get(cacheKey));
  }

  @Override
  public void expire(K cacheKey) {
    cache.remove(cacheKey);
    // Release latch if anything is waiting for it.
    CountDownLatch expiryLatch = expiryLatchByCacheKey.get(cacheKey);
    if (expiryLatch != null) {
      expiryLatch.countDown();
    }
  }

  @Override
  public void expireAll() {
    cache.clear();
    expiryLatchByCacheKey.forEach((k, expiryLatch) -> expiryLatch.countDown());
  }

  public Map<K, V> getCache() {
    return Collections.unmodifiableMap(cache);
  }

  public boolean waitForExpiry(K cacheKey) throws InterruptedException {
    CountDownLatch expiryLatch = expiryLatchByCacheKey.get(cacheKey);
    if (expiryLatch == null) {
      throw new IllegalArgumentException("Expiry latch for cache key not found: " + cacheKey);
    }
    return expiryLatch.await(5, TimeUnit.SECONDS);
  }

  public boolean waitForExpiry(Collection<K> cacheKeys) throws InterruptedException {
    for (K cacheKey : cacheKeys) {
      if (!waitForExpiry(cacheKey)) {
        // Timeout elapsed.
        return false;
      }
      ;
    }
    return true;
  }
}
