package io.github.joeljeremy.externalizedproperties.core.internal.caching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubCacheStrategy;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Nested
public class ExpiringCacheStrategyTests {
  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when decorated argument is null")
    void test1() {
      Duration duration = Duration.ofSeconds(30);
      assertThrows(
          IllegalArgumentException.class, () -> new ExpiringCacheStrategy<>(null, duration));
    }

    @Test
    @DisplayName("should throw when cache item lifetime argument is null")
    void test2() {
      StubCacheStrategy<?, ?> cacheStrategy = new StubCacheStrategy<>();

      assertThrows(
          IllegalArgumentException.class, () -> new ExpiringCacheStrategy<>(cacheStrategy, null));
    }
  }

  @Nested
  class CacheMethod {
    @Test
    @DisplayName("should cache value to the decorated cache strategy")
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(decorated, Duration.ofSeconds(30));

      cacheStrategy.cache(cacheKey, cacheValue);

      assertEquals(cacheValue, decorated.getCache().get(cacheKey));
    }

    @Test
    @DisplayName("should expire cache entries when cache item lifetime elapses")
    void test2() throws Throwable {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(decorated, Duration.ofMillis(100));

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
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      StubCacheStrategy<String, String> decorated = new StubCacheStrategy<>();
      decorated.cache(cacheKey, cacheValue);

      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(decorated, Duration.ofSeconds(30));

      Optional<String> cachedPropertyValue = cacheStrategy.get(cacheKey);

      assertTrue(cachedPropertyValue.isPresent());
      assertSame(cacheValue, cachedPropertyValue.get());
    }

    @Test
    @DisplayName(
        "should return empty Optional when key is not found in the decorated cache strategy")
    void test2() {
      String cacheKey = "cache.key";

      StubCacheStrategy<String, String> empty = new StubCacheStrategy<>();

      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(empty, Duration.ofSeconds(30));

      Optional<String> cachedPropertyValue = cacheStrategy.get(cacheKey);

      assertFalse(cachedPropertyValue.isPresent());
    }
  }

  @Nested
  class ExpireMethod {
    @Test
    @DisplayName("should expire cached value from the decorated cache strategy")
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "cache.value";

      StubCacheStrategy<String, String> cache = new StubCacheStrategy<>();
      cache.cache(cacheKey, cacheValue);

      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(cache, Duration.ofSeconds(30));

      cacheStrategy.expire(cacheKey);

      // Deleted from cache map.
      assertFalse(cache.getCache().containsKey(cacheKey));
    }
  }

  @Nested
  class ExpireAllMethod {
    @Test
    @DisplayName("should expire all cached values from the decorated cache strategy")
    void test1() {
      String cacheKey = "cache.key";
      String cacheValue = "property.value";

      StubCacheStrategy<String, String> cache = new StubCacheStrategy<>();
      cache.cache(cacheKey, cacheValue);

      CacheStrategy<String, String> cacheStrategy =
          new ExpiringCacheStrategy<>(cache, Duration.ofSeconds(30));

      cacheStrategy.expireAll();

      // All items deleted from cache map.
      assertTrue(cache.getCache().isEmpty());
    }
  }
}
