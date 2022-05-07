package io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class ConcurrentHashMapCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should not throw when invoking default constructor")
        public void test1() {
            assertDoesNotThrow(
                () -> new ConcurrentHashMapCacheStrategy<>()
            );
        }

        @Test
        @DisplayName("should throw when cache argument argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new ConcurrentHashMapCacheStrategy<>(null)
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
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentHashMapCacheStrategy<>();
    
            cacheStrategy.cache(cacheKey, cacheValue);
    
            assertEquals(
                cacheValue, 
                cacheStrategy.get(cacheKey).get()
            );
        }
    }

    @Nested
    class GetMethod {
        @Test
        @DisplayName("should return cached value from the cache map")
        public void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
            
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentHashMapCacheStrategy<>();
            
            cacheStrategy.cache(cacheKey, cacheValue);
    
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
    
            // Empty cache.
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentHashMapCacheStrategy<>();
    
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
    
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentHashMapCacheStrategy<>();

            // Cache and assert.
            cacheStrategy.cache(cacheKey, cacheValue);
            assertTrue(cacheStrategy.get(cacheKey).isPresent());
    
            // Expire all cached items.
            cacheStrategy.expire(cacheKey);
    
            // Deleted from cache map.
            assertFalse(cacheStrategy.get(cacheKey).isPresent());
        }
    }

    @Nested
    class ExpireAllMethod {
        @Test
        @DisplayName("should expire all cached values from the cache map")
        public void test1() {
            String cacheKey1 = "cache.key.1";
            String cacheValue1 = "property.value.1";
            String cacheKey2 = "cache.key.2";
            String cacheValue2 = "property.value.2";
            String cacheKey3 = "cache.key.3";
            String cacheValue3 = "property.value.3";
            
            CacheStrategy<String, String> cacheStrategy = 
                new ConcurrentHashMapCacheStrategy<>();
            
            // Cache and assert.
            cacheStrategy.cache(cacheKey1, cacheValue1);
            cacheStrategy.cache(cacheKey2, cacheValue2);
            cacheStrategy.cache(cacheKey3, cacheValue3);
            assertTrue(cacheStrategy.get(cacheKey1).isPresent());
            assertTrue(cacheStrategy.get(cacheKey2).isPresent());
            assertTrue(cacheStrategy.get(cacheKey3).isPresent());
    
            // Expire all cached items.
            cacheStrategy.expireAll();
    
            assertFalse(cacheStrategy.get(cacheKey1).isPresent());
            assertFalse(cacheStrategy.get(cacheKey2).isPresent());
            assertFalse(cacheStrategy.get(cacheKey3).isPresent());
        }
    }
}