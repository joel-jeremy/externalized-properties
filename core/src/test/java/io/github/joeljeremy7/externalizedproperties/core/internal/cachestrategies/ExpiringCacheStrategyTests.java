package io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubCacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class ExpiringCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ExpiringCacheStrategy<>(null, Duration.ofSeconds(30))
            );
        }

        @Test
        @DisplayName("should throw when cache item lifetime argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ExpiringCacheStrategy<>(new StubCacheStrategy<>(), null)
            );
        }
    }

    @Nested
    class CacheMethod {
        @Test
        @DisplayName("should cache value to the decorated cache strategy")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    decorated,
                    Duration.ofSeconds(30)
                );
    
            cacheStrategy.cache(cacheKey, cacheValue);
    
            assertEquals(
                cacheValue, 
                decorated.getCache().get(cacheKey)
            );
        }

        @Test
        @DisplayName("should expire cache entries when cache item lifetime elapses")
        public void test2() throws Throwable {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    decorated,
                    Duration.ofMillis(100)
                );
    
            cacheStrategy.cache(cacheKey, cacheValue);

            // Result was cached.
            assertSame(decorated.getCache().get(cacheKey), cacheValue);

            // Wait for expiry method to be invoked.
            assertTrue(decorated.waitForExpiry(cacheKey));

            // Cache entry was removed.
            assertTrue(decorated.getCache().isEmpty());
        }
    }

    @Nested
    class GetMethod {
        @Test
        @DisplayName("should return cached value from the decorated cache strategy")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
            decorated.cache(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    decorated,
                    Duration.ofSeconds(30)
                );
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertTrue(cachedPropertyValue.isPresent());
            assertSame(
                cacheValue, 
                cachedPropertyValue.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when key is not found in the decorated cache strategy"
        )
        public void test2() {
            String cacheKey = "cache.key";
    
            StubCacheStrategy<String, String> empty = new StubCacheStrategy<>();
    
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    empty,
                    Duration.ofSeconds(30)
                );
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertFalse(cachedPropertyValue.isPresent());
        }
    }

    @Nested
    class ExpireMethod {
        @Test
        @DisplayName("should expire cached value from the decorated cache strategy")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            StubCacheStrategy<String, String> cache = new StubCacheStrategy<>();
            cache.cache(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    cache,
                    Duration.ofSeconds(30)
                );
    
            cacheStrategy.expire(cacheKey);
    
            // Deleted from cache map.
            assertFalse(cache.getCache().containsKey(cacheKey));
        }
    }

    @Nested
    class ExpireAllMethod {
        @Test
        @DisplayName("should expire all cached values from the decorated cache strategy")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "property.value";
    
            StubCacheStrategy<String, String> cache = new StubCacheStrategy<>();
            cache.cache(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ExpiringCacheStrategy<>(
                    cache,
                    Duration.ofSeconds(30)
                );
    
            cacheStrategy.expireAll();
    
            // All items deleted from cache map.
            assertTrue(cache.getCache().isEmpty());
        }
    }
}