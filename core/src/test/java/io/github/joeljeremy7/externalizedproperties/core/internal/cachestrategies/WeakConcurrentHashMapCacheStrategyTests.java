package io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Nested
public class WeakConcurrentHashMapCacheStrategyTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should not throw when invoking default constructor")
        void test1() {
            assertDoesNotThrow(
                () -> new WeakConcurrentHashMapCacheStrategy<>()
            );
        }

        @Test
        @DisplayName("should throw when cache argument argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new WeakConcurrentHashMapCacheStrategy<>(null)
            );
        }
    }

    @Nested
    class WeakKeyReferenceTests {
        @Test
        @DisplayName("should automatically remove cache key when weak references are cleared")
        void test1() throws InterruptedException {
            CacheKey cacheKey1 = new CacheKey("cache.key.1");
            CacheKey cacheKey2 = new CacheKey("cache.key.2");
    
            CacheStrategy<CacheKey, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();
    
            // Cache and assert.
            cacheStrategy.cache(cacheKey1, "cache.value.1");
            cacheStrategy.cache(cacheKey2, "cache.value.2");
            assertTrue(cacheStrategy.get(cacheKey1).isPresent());
            assertTrue(cacheStrategy.get(cacheKey2).isPresent());

            // Clear references.
            cacheKey1 = null;
            cacheKey2 = null;

            // Original key references were nulled so create a new reference
            // with matching string keys for lookups.
            CacheKey cacheKey1Lookup = new CacheKey("cache.key.1");
            CacheKey cacheKey2Lookup = new CacheKey("cache.key.2");

            assertTimeoutPreemptively(Duration.ofSeconds(86400), () -> {
                // Wait for GC to clear references.
                while (cacheStrategy.get(cacheKey1Lookup).isPresent() ||
                        cacheStrategy.get(cacheKey2Lookup).isPresent()) {
                    System.gc();
                }
            });
            
            assertFalse(cacheStrategy.get(cacheKey1Lookup).isPresent());
            assertFalse(cacheStrategy.get(cacheKey2Lookup).isPresent());
        }
    }

    @Nested
    class CacheMethod {
        @Test
        @DisplayName("should cache value to the cache map")
        void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();
    
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
        void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();

            // Cache.
            cacheStrategy.cache(cacheKey, cacheValue);
    
            // Retrieve.
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
        void test2() {
            String cacheKey = "cache.key";
    
            // Empty cache.
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();
    
            Optional<String> cachedPropertyValue = 
                cacheStrategy.get(cacheKey);
    
            assertFalse(cachedPropertyValue.isPresent());
        }
    }

    @Nested
    class ExpireMethod {
        @Test
        @DisplayName("should expire cached value from the cache map")
        void test1() {
            String cacheKey = "cache.key";
            String cacheValue = "cache.value";
    
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();

            // Cache and assert.
            cacheStrategy.cache(cacheKey, cacheValue);
            assertTrue(cacheStrategy.get(cacheKey).isPresent());
    
            // Expire the cached key.
            cacheStrategy.expire(cacheKey);
    
            // Deleted from cache map.
            assertFalse(cacheStrategy.get(cacheKey).isPresent());
        }
    }

    @Nested
    class ExpireAllMethod {
        @Test
        @DisplayName("should expire all cached values from the cache map")
        void test1() {
            String cacheKey1 = "cache.key.1";
            String cacheValue1 = "property.value.1";
            String cacheKey2 = "cache.key.2";
            String cacheValue2 = "property.value.2";
            String cacheKey3 = "cache.key.3";
            String cacheValue3 = "property.value.3";
            
            CacheStrategy<String, String> cacheStrategy = 
                new WeakConcurrentHashMapCacheStrategy<>();
            
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

    @Nested
    class WeakKeyTests {
        @Nested
        class HashCodeMethod {
            @Test
            @DisplayName("should return hash code of the referent")
            void test1() {
                String referent = "referent";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent);
                assertEquals(referent.hashCode(), key.hashCode());
            }

            @Test
            @DisplayName("should return the same of hash code everytime")
            void test2() {
                String referent = "referent";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent);
                int hashCode1 = key.hashCode();
                int hashCode2 = key.hashCode();
                assertEquals(hashCode1, hashCode2);
            }

            @Test
            @DisplayName("should return different hash codes for different referents")
            void test3() {
                String referent1 = "referent1";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent1);

                String referent2 = "referent2";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> otherKey = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent2);
                
                assertNotEquals(key.hashCode(), otherKey.hashCode());
            }
        }
        
        @Nested
        class EqualsMethod {
            @Test
            @DisplayName("should return true when WeakKey referents are equal")
            void test1() {
                String referent = "referent";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent);

                WeakConcurrentHashMapCacheStrategy.WeakKey<String> sameReferentKey = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent);

                assertTrue(key.equals(sameReferentKey));
            }

            @Test
            @DisplayName("should return false when WeakKey referents are not equal")
            void test2() {
                String referent1 = "referent1";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent1);

                String referent2 = "referent2";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> otherKey = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent2);

                assertFalse(key.equals(otherKey));
            }

            @Test
            @DisplayName("should return false when object is not a WeakKey")
            void test3() {
                String referent = "referent";
                WeakConcurrentHashMapCacheStrategy.WeakKey<String> key = 
                    WeakConcurrentHashMapCacheStrategy.WeakKey.forLookup(referent);

                assertFalse(key.equals(new Object()));
            }
        }
    }

    public static class CacheKey {
        private final String key;
        
        public CacheKey(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj instanceof CacheKey) {
                CacheKey other = (CacheKey)obj;
                return Objects.equals(other.key, key);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }
}