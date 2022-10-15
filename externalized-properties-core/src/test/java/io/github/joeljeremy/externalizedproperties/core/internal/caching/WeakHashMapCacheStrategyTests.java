package io.github.joeljeremy.externalizedproperties.core.internal.caching;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Nested
public class WeakHashMapCacheStrategyTests {
  @Nested
  class Constructor {
    @Test
    @DisplayName("should not throw when invoking default constructor")
    void test1() {
      assertDoesNotThrow(() -> new WeakHashMapCacheStrategy<>());
    }

    @Test
    @DisplayName("should throw when cache argument argument is null")
    void test2() {
      assertThrows(IllegalArgumentException.class, () -> new WeakHashMapCacheStrategy<>(null));
    }
  }

  @Nested
  class WeakKeyReferenceTests {
    @Test
    @DisplayName("should automatically remove cache key when weak references are cleared")
    void test1() {
      CacheKey cacheKey1 = new CacheKey("cache.key.1");
      CacheKey cacheKey2 = new CacheKey("cache.key.2");

      CacheStrategy<CacheKey, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

      // Cache and assert.
      cacheStrategy.cache(cacheKey1, "cache.value.1");
      cacheStrategy.cache(cacheKey2, "cache.value.2");
      assertTrue(cacheStrategy.get(cacheKey1).isPresent());
      assertTrue(cacheStrategy.get(cacheKey2).isPresent());

      // Clear references.
      cacheKey1 = null;
      cacheKey2 = null;

      // Original key references were null so they can be cleared/collected.
      CacheKey cacheKey1Copy = new CacheKey("cache.key.1");
      CacheKey cacheKey2Copy = new CacheKey("cache.key.2");

      assertTimeoutPreemptively(
          Duration.ofMinutes(10),
          () -> {
            // Wait for GC to clear references.
            while (cacheStrategy.get(cacheKey1Copy).isPresent()
                || cacheStrategy.get(cacheKey1Copy).isPresent()) {
              System.gc();
            }
          });

      assertFalse(cacheStrategy.get(cacheKey1Copy).isPresent());
      assertFalse(cacheStrategy.get(cacheKey2Copy).isPresent());
    }
  }

  @Nested
  class CacheMethod {
    @Test
    @DisplayName("should cache value to the cache map")
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      CacheStrategy<String, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

      cacheStrategy.cache(cacheKey, cacheValue);

      assertEquals(cacheValue, cacheStrategy.get(cacheKey).get());
    }
  }

  @Nested
  class GetMethod {
    @Test
    @DisplayName("should return cached value from the cache map")
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      CacheStrategy<String, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

      cacheStrategy.cache(cacheKey, cacheValue);

      Optional<String> cachedPropertyValue = cacheStrategy.get(cacheKey);

      assertTrue(cachedPropertyValue.isPresent());
      assertSame(cacheValue, cachedPropertyValue.get());
    }

    @Test
    @DisplayName("should return empty Optional when key is not found in cache map")
    void test2() {
      String cacheKey = "cache.key";

      // Empty cache.
      CacheStrategy<String, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

      Optional<String> cachedPropertyValue = cacheStrategy.get(cacheKey);

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

      CacheStrategy<String, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

      // Cache and assert.
      cacheStrategy.cache(cacheKey, cacheValue);
      assertTrue(cacheStrategy.get(cacheKey).isPresent());

      // Expire the cache key.
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

      CacheStrategy<String, String> cacheStrategy = new WeakHashMapCacheStrategy<>();

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
        CacheKey other = (CacheKey) obj;
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
