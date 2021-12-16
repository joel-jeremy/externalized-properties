package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubProxyMethodInfo;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubInvocationHandler;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EagerLoadingInvocationHandlerTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated invocation handler argument is null")
        public void test1() {
            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .withDefaults()
                    .build();

            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            assertThrows(
                IllegalArgumentException.class,
                () -> new EagerLoadingInvocationHandler(
                    null, 
                    externalizedProperties, 
                    BasicProxyInterface.class, 
                    cacheStrategy  
                )
            );
        }

        @Test
        @DisplayName("should throw when externalized properties argument is null")
        public void test2() {
            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            assertThrows(
                IllegalArgumentException.class,
                () -> new EagerLoadingInvocationHandler(
                    new StubInvocationHandler(), 
                    null, 
                    BasicProxyInterface.class, 
                    cacheStrategy  
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is null")
        public void test3() {
            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .withDefaults()
                    .build();

            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            assertThrows(
                IllegalArgumentException.class,
                () -> new EagerLoadingInvocationHandler(
                    new StubInvocationHandler(), 
                    externalizedProperties, 
                    null, 
                    cacheStrategy  
                )
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        public void test4() {
            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .withDefaults()
                    .build();

            assertThrows(
                IllegalArgumentException.class,
                () -> new EagerLoadingInvocationHandler(
                    new StubInvocationHandler(), 
                    externalizedProperties, 
                    BasicProxyInterface.class, 
                    null
                )
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        public void test5() {
            StubResolver resolver = new StubResolver();

            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(resolver)
                    .build();

            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            // Eager loads properties.
            new EagerLoadingInvocationHandler(
                new StubInvocationHandler(), 
                externalizedProperties, 
                BasicProxyInterface.class, 
                cacheStrategy
            );

            for (Method method : BasicProxyInterface.class.getDeclaredMethods()) {
                ExternalizedProperty externalizedProperty =
                    method.getAnnotation(ExternalizedProperty.class);
                if (externalizedProperty != null) {
                    Optional<Object> cachedValue = cacheStrategy.get(method);

                    assertTrue(cachedValue.isPresent());
                    assertEquals(
                        resolver.resolvedProperties().get(externalizedProperty.value()), 
                        cachedValue.get()
                    );
                }
            }
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

            StubResolver resolver = new StubResolver();

            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(resolver)
                    .build();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            Class<BasicProxyInterface> proxyInterface = BasicProxyInterface.class;

            // Eager loads properties.
            EagerLoadingInvocationHandler invocationHandler = new EagerLoadingInvocationHandler(
                decorated, 
                externalizedProperties, 
                proxyInterface, 
                cacheStrategy
            );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
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

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();

            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(resolver)
                    .build();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );
            
            Class<BasicProxyInterface> proxyInterface = BasicProxyInterface.class;

            EagerLoadingInvocationHandler invocationHandler = 
                new EagerLoadingInvocationHandler(
                    decorated,
                    externalizedProperties,
                    proxyInterface,
                    cacheStrategy
                );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
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
            "should return null not cache" + 
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

            StubResolver resolver = new StubResolver(
                StubResolver.NULL_VALUE_RESOLVER
            );

            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(resolver)
                    .build();

            Class<BasicProxyInterface> proxyInterface = BasicProxyInterface.class;

            EagerLoadingInvocationHandler invocationHandler = 
                new EagerLoadingInvocationHandler(
                    decorated,
                    externalizedProperties,
                    proxyInterface,
                    cacheStrategy
                );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
                stubMethod, 
                new Object[0]
            );

            assertNull(result);
            // Not cached.
            assertFalse(cacheStrategy.get(stubMethod).isPresent());
        }

        @SuppressWarnings("unchecked")
        private <T> T stubProxy(Class<T> proxyInterface, StubInvocationHandler decorated) {
            return (T)Proxy.newProxyInstance(
                proxyInterface.getClassLoader(), 
                new Class<?>[] { proxyInterface }, 
                decorated
            );
        }

        private Method stubMethod() {
            return StubProxyMethodInfo.getMethod(
                BasicProxyInterface.class, 
                "property"
            );
        }
    }
}
