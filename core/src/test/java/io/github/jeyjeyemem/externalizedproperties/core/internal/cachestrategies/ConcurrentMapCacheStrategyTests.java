package io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class ConcurrentMapCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should not throw when invoking default constructor")
        public void test1() {
            assertDoesNotThrow(
                () -> new ConcurrentMapCacheStrategy<>()
            );
        }

        @Test
        @DisplayName("should throw when cache argument argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConcurrentMapCacheStrategy<>(null)
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
    
            ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentMapCacheStrategy<>(cache);
    
            cacheStrategy.cache(cacheKey, cacheValue);
    
            assertEquals(
                cacheValue, 
                cache.get(cacheKey)
            );
        }
    }

    @Nested
    class GetFromCacheMethod {
        @Test
        @DisplayName("should return cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentMapCacheStrategy<>(cache);
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.getFromCache(cacheKey);
    
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
    
            ConcurrentMap<String, String> empty = new ConcurrentHashMap<>();
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentMapCacheStrategy<>(empty);
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.getFromCache(cacheKey);
    
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
    
            ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentMapCacheStrategy<>(cache);
    
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
    
            ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();
            cache.put(cacheKey, cacheValue);
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentMapCacheStrategy<>(cache);
    
            cacheStrategy.expireAll();
    
            // All items deleted from cache map.
            assertTrue(cache.isEmpty());
        }
    }
}