package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Caching strategy which uses a {@link ConcurrentMap} as cache and whose keys are weakly held.
 */
public class WeakConcurrentMapCacheStrategy<K, V> implements CacheStrategy<K, V> {

    private final ReferenceQueue<K> referenceQueue = new ReferenceQueue<>();
    private final ConcurrentMap<WeakKey<K>, V> cache;

    /**
     * Default constructor for building a cache strategy that uses an 
     * internal {@link ConcurrentHashMap} cache.
     */
    public WeakConcurrentMapCacheStrategy() {
        this(new ConcurrentHashMap<>());
    }

    /**
     * Package-private constructor.
     */
    WeakConcurrentMapCacheStrategy(ConcurrentMap<WeakKey<K>, V> cache) {
        this.cache = requireNonNull(cache, "cache");
    }

    /** {@inheritDoc} */
    @Override
    public void cache(K cacheKey, V cacheValue) {
        cache.putIfAbsent(new WeakKey<>(cacheKey, referenceQueue), cacheValue);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<V> get(K cacheKey) {
        purgeKeys();
        return Optional.ofNullable(
            cache.get(new WeakKey<>(cacheKey))
        );
    }

    /** {@inheritDoc} */
    @Override
    public void expire(K cacheKey) {
        purgeKeys();
        cache.remove(new WeakKey<>(cacheKey));
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
     */
    static class WeakKey<K> extends WeakReference<K> {
        private final int hashCode;

        /**
         * Constructor.
         * 
         * @param referent The referent.
         * @param referenceQueue The reference queue.
         */
        public WeakKey(K referent, ReferenceQueue<? super K> referenceQueue) {
            super(referent, referenceQueue);
            hashCode = referent.hashCode();
        }

        /**
         * Constructor.
         * 
         * @param referent The referent.
         */
        public WeakKey(K referent) {
            this(referent, null);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WeakKey) {
                @SuppressWarnings("unchecked")
                WeakKey<K> other = (WeakKey<K>)obj;
                if (Objects.equals(get(), other.get())) {
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
