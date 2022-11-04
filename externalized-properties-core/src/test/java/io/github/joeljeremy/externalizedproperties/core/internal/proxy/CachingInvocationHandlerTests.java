package io.github.joeljeremy.externalizedproperties.core.internal.proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationCacheKey;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubCacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubInvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CachingInvocationHandlerTests {
  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when decorated argument is null")
    void test1() {
      StubCacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();
      assertThrows(
          IllegalArgumentException.class, () -> new CachingInvocationHandler(null, cacheStrategy));
    }

    @Test
    @DisplayName("should throw when cache strategy argument is null")
    void test2() {
      StubInvocationHandler decorated = new StubInvocationHandler();
      assertThrows(
          IllegalArgumentException.class, () -> new CachingInvocationHandler(decorated, null));
    }
  }

  @Nested
  class InvokeMethod {
    @Test
    @DisplayName("should return cached property values")
    void test1() throws Throwable {
      Method stubMethod = stubMethod();

      StubCacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();
      // Add to cache.
      cacheStrategy.cache(new InvocationCacheKey(stubMethod), "cached-value");

      // Always return the same string for any invoked proxy method.
      StubInvocationHandler decorated =
          new StubInvocationHandler(i -> "value-from-decorated-handler");

      CachingInvocationHandler cachingInvocationHandler =
          new CachingInvocationHandler(decorated, cacheStrategy);

      Object result =
          cachingInvocationHandler.invoke(
              stubProxy(decorated),
              stubMethod,
              // null because proxies give null instead of empty array
              // when there are no arguments...
              null);

      assertNotNull(result);
      assertEquals("cached-value", result);
      assertSame(cacheStrategy.getCache().get(new InvocationCacheKey(stubMethod)), result);
    }

    @Test
    @DisplayName(
        "should resolve uncached property values from decorated invocation handler"
            + "and cache the resolved value")
    void test2() throws Throwable {
      Method stubMethod = stubMethod();

      // Always return the same string for any invoked proxy method.
      StubInvocationHandler decorated =
          new StubInvocationHandler(i -> "value-from-decorated-handler");

      // No cached results.
      CacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();

      CachingInvocationHandler cachingInvocationHandler =
          new CachingInvocationHandler(decorated, cacheStrategy);

      Object result =
          cachingInvocationHandler.invoke(
              stubProxy(decorated),
              stubMethod,
              // null because proxies give null instead of empty array
              // when there are no arguments...
              null);

      assertNotNull(result);
      // Resolved from decorated invocation handler.
      assertEquals("value-from-decorated-handler", result);
      // Resolved value was cached.
      assertTrue(cacheStrategy.get(new InvocationCacheKey(stubMethod)).isPresent());
    }

    @Test
    @DisplayName(
        "should not cache and return null "
            + "when property could not be resolved from decorated invocation handler")
    void test3() throws Throwable {
      Method stubMethod = stubMethod();

      // Always return null.
      StubInvocationHandler decorated =
          new StubInvocationHandler(StubInvocationHandler.NULL_DELEGATE);

      // No cached results.
      CacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();

      CachingInvocationHandler cachingInvocationHandler =
          new CachingInvocationHandler(decorated, cacheStrategy);

      Object result =
          cachingInvocationHandler.invoke(
              stubProxy(decorated),
              stubMethod,
              // null because proxies give null instead of empty array
              // when there are no arguments...
              null);

      assertNull(result);
      // Not cached.
      assertFalse(cacheStrategy.get(new InvocationCacheKey(stubMethod)).isPresent());
    }

    @Test
    @DisplayName("should throw when decorated invocation handler throws")
    void test4() throws Throwable {
      Method stubMethod = stubMethod();

      // Always return the same string for any invoked proxy method.
      StubInvocationHandler decorated =
          new StubInvocationHandler(StubInvocationHandler.THROWING_DELEGATE);

      CachingInvocationHandler cachingInvocationHandler =
          new CachingInvocationHandler(decorated, new StubCacheStrategy<>());

      Object stubProxy = stubProxy(decorated);

      assertThrows(
          ExternalizedPropertiesException.class,
          () ->
              cachingInvocationHandler.invoke(
                  stubProxy,
                  stubMethod,
                  // null because proxies give null instead of empty array
                  // when there are no arguments...
                  null));
    }
  }

  private StubProxyInterface stubProxy(StubInvocationHandler decorated) {
    return (StubProxyInterface)
        Proxy.newProxyInstance(
            StubProxyInterface.class.getClassLoader(),
            new Class<?>[] {StubProxyInterface.class},
            decorated);
  }

  private Method stubMethod() {
    return MethodUtils.getMethod(StubProxyInterface.class, StubProxyInterface::methodName);
  }

  static interface StubProxyInterface {
    String methodName();
  }
}
