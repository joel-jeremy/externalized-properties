package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubExternalizedPropertyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubInvocationHandler;
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
                    new StubCacheStrategy<>()
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
                    cacheStrategy
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
                    cacheStrategy
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
                    cacheStrategy
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

        private StubProxyInterface stubProxy(StubInvocationHandler decorated) {
            return (StubProxyInterface)Proxy.newProxyInstance(
                StubProxyInterface.class.getClassLoader(), 
                new Class<?>[] { StubProxyInterface.class }, 
                decorated
            );
        }

        private Method stubMethod() {
            return StubExternalizedPropertyMethodInfo.getMethod(
                StubProxyInterface.class, 
                "methodName"
            );
        }
    }

    private static interface StubProxyInterface {
        String methodName();
    }
}
