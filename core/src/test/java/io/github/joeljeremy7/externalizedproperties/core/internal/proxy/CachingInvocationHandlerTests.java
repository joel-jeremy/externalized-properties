package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubInvocationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachingInvocationHandlerTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingInvocationHandler(
                    null,
                    new StubCacheStrategy<>(),
                    StubProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingInvocationHandler(
                    new StubInvocationHandler(),
                    null,
                    StubProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingInvocationHandler(
                    new StubInvocationHandler(),
                    new StubCacheStrategy<>(),
                    null
                )
            );
        }
    }

    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should return cached property values")
        public void test1() throws Throwable {
            Method stubMethod = stubMethod();

            StubCacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();
            // Add to cache.
            cacheStrategy.cache(stubMethod, "cached-value");

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    cacheStrategy,
                    StubProxyInterface.class
                );
            
            Object result = cachingInvocationHandler.invoke(
                stubProxy(decorated), 
                stubMethod, 
                new Object[0]
            );

            assertNotNull(result);
            assertEquals("cached-value", result);
            assertSame(
                cacheStrategy.getCache().get(stubMethod), 
                result
            );
        }

        @Test
        @DisplayName(
            "should resolve uncached property values from decorated invocation handler" +
            "and cache the resolved value"
        )
        public void test2() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    cacheStrategy,
                    StubProxyInterface.class
                );
            
            Object result = cachingInvocationHandler.invoke(
                stubProxy(decorated), 
                stubMethod, 
                new Object[0]
            );

            assertNotNull(result);
            // Resolved from decorated invocation handler.
            assertEquals("value-from-decorated-handler", result);
            // Resolved value was cached.
            assertTrue(cacheStrategy.get(stubMethod).isPresent());
        }

        @Test
        @DisplayName(
            "should not cache and return null " + 
            "when property could not be resolved from decorated invocation handler"
        )
        public void test3() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return null.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.NULL_HANDLER
            );

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    cacheStrategy,
                    StubProxyInterface.class
                );
            
            Object result = cachingInvocationHandler.invoke(
                stubProxy(decorated), 
                stubMethod, 
                new Object[0]
            );

            assertNull(result);
            // Not cached.
            assertFalse(cacheStrategy.get(stubMethod).isPresent());
        }
        @Test
        @DisplayName("should throw when decorated invocation handler throws")
        public void test4() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.THROWING_HANDLER
            );

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    new StubCacheStrategy<>(),
                    StubProxyInterface.class
                );
            
            assertThrows(
            ExternalizedPropertiesException.class, 
                () ->cachingInvocationHandler.invoke(
                    stubProxy(decorated), 
                    stubMethod, 
                    new Object[0]
                )
            );
        }
    }

    private StubProxyInterface stubProxy(StubInvocationHandler decorated) {
        return (StubProxyInterface)Proxy.newProxyInstance(
            StubProxyInterface.class.getClassLoader(), 
            new Class<?>[] { StubProxyInterface.class }, 
            decorated
        );
    }

    private Method stubMethod() {
        return ProxyMethodUtils.getMethod(
            StubProxyInterface.class, 
            StubProxyInterface::methodName
        );
    }

    static interface StubProxyInterface {
        String methodName();
    }
}
