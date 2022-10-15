package io.github.joeljeremy.externalizedproperties.core.internal.caching;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Caching strategy which uses a {@link ConcurrentMap} as cache and whose keys are weakly held. */
public class WeakConcurrentHashMapCacheStrategy<K, V> implements CacheStrategy<K, V> {

  private final ReferenceQueue<K> referenceQueue = new ReferenceQueue<>();
  private final ConcurrentMap<WeakKey<K>, V> cache;

  /**
   * Default constructor for building a cache strategy that uses an internal {@link
   * ConcurrentHashMap} cache.
   */
  public WeakConcurrentHashMapCacheStrategy() {
    this(new ConcurrentHashMap<>());
  }

  /** Package-private constructor. */
  WeakConcurrentHashMapCacheStrategy(ConcurrentMap<WeakKey<K>, V> cache) {
    this.cache = requireNonNull(cache, "cache");
  }

  /** {@inheritDoc} */
  @Override
  public void cache(K cacheKey, V cacheValue) {
    cache.putIfAbsent(WeakKey.forWrite(cacheKey, referenceQueue), cacheValue);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<V> get(K cacheKey) {
    purgeKeys();
    return Optional.ofNullable(cache.get(WeakKey.forLookup(cacheKey)));
  }

  /** {@inheritDoc} */
  @Override
  public void expire(K cacheKey) {
    purgeKeys();
    cache.remove(WeakKey.forLookup(cacheKey));
  }

  /** {@inheritDoc} */
  @Override
  public void expireAll() {
    purgeKeys();
    cache.clear();
  }

  private void purgeKeys() {
    Reference<? extends K> reference;
    while ((reference = referenceQueue.poll()) != null) {
      cache.remove(reference);
    }
  }

  /**
   * Package-private weak map key.
   *
   * @param <K> The key type.
   */
  static class WeakKey<K> extends WeakReference<K> {
    private final int hashCode;

    /**
     * Private constructor.
     *
     * @param referent The referent.
     * @param referenceQueue The reference queue.
     */
    private WeakKey(K referent, @Nullable ReferenceQueue<? super K> referenceQueue) {
      super(referent, referenceQueue);
      hashCode = Objects.hashCode(referent);
    }

    /**
     * Create a {@link WeakKey} to be used when doing lookups in the weak map.
     *
     * @param <K> The key type.
     * @param referent The referent.
     * @return A {@link WeakKey} to be used in map lookups.
     */
    static <K> WeakKey<K> forLookup(K referent) {
      return new WeakKey<>(referent, null);
    }

    /**
     * Create a {@link WeakKey} to be used when writing to the weak map.
     *
     * @param <K> The key type.
     * @param referent The referent.
     * @param referenceQueue The reference queue.
     * @return A {@link WeakKey} to be used in map writes.
     */
    static <K> WeakKey<K> forWrite(K referent, @Nullable ReferenceQueue<? super K> referenceQueue) {
      return new WeakKey<>(referent, referenceQueue);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof WeakKey) {
        @SuppressWarnings("unchecked")
        WeakKey<K> other = (WeakKey<K>) obj;
        if (Objects.equals(super.get(), other.get())) {
          return true;
        }
      }
      return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
