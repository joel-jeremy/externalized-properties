package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.WeakHashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class WeakHashMapCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should not throw when invoking default constructor")
        public void test1() {
            assertDoesNotThrow(
                () -> new WeakHashMapCacheStrategy<>()
            );
        }

        @Test
        @DisplayName("should throw when cache argument argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new WeakHashMapCacheStrategy<>(null)
            );
        }
    }

    @Nested
    class CacheMethod {
        @Test
        @DisplayName("should cache value to the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            WeakHashMap<String, String> cache = new WeakHashMap<>();
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(cache);
    
            cacheStrategy.cache(cacheKey, cacheValue);
    
            assertEquals(
                cacheValue, 
                cache.get(cacheKey)
            );
        }

        @Test
        @DisplayName("should automatically remove cache key when weak references are cleared")
        public void test2() throws InterruptedException {
            // Use String constructor to explicitly create
            // new String instance and prevent string interning.
            // This allows GC to clear this reference when set to null.
            String cacheKey1 = new String("cache.key.1");
            String cacheKey2 = new String("cache.key.2");
    
            WeakHashMap<String, String> cache = new WeakHashMap<>();
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(cache);
    
            cacheStrategy.cache(cacheKey1, "cache.value.1");
            cacheStrategy.cache(cacheKey2, "cache.value.2");

            assertTrue(cache.containsKey(cacheKey1));
            assertTrue(cache.containsKey(cacheKey2));

            // Clear references.
            cacheKey1 = null;
            cacheKey2 = null;

            assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
                // Wait for GC to clear references.
                while (!cache.isEmpty()) {
                    System.gc();
                }
            });
            
            assertTrue(cache.isEmpty());
        }
    }

    @Nested
    class GetMethod {
        @Test
        @DisplayName("should return cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            WeakHashMap<String, String> cache = new WeakHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(cache);
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertTrue(cachedPropertyValue.isPresent());
            assertSame(
                cacheValue, 
                cachedPropertyValue.get()
            );
        }

        @Test
        @DisplayName("should return empty Optional when key is not found in cache map")
        public void test2() {
            String cacheKey = "cache.key";
    
            WeakHashMap<String, String> empty = new WeakHashMap<>();
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(empty);
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertFalse(cachedPropertyValue.isPresent());
        }
    }

    @Nested
    class ExpireMethod {
        @Test
        @DisplayName("should expire cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            WeakHashMap<String, String> cache = new WeakHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(cache);
    
            cacheStrategy.expire(cacheKey);
    
            // Deleted from cache map.
            assertFalse(cache.containsKey(cacheKey));
        }
    }

    @Nested
    class ExpireAllMethod {
        @Test
        @DisplayName("should expire all cached values from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "property.value";
    
            WeakHashMap<String, String> cache = new WeakHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakHashMapCacheStrategy<>(cache);
    
            cacheStrategy.expireAll();
    
            // All items deleted from cache map.
            assertTrue(cache.isEmpty());
        }
    }
}