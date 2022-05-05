package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubInvocationHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
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
    class EagerLoadFactoryMethod {
        @Test
        @DisplayName("should throw when decorated invocation handler argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> EagerLoadingInvocationHandler.eagerLoad(
                    null,  
                    new StubCacheStrategy<>(),
                    EagerLoadingProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> EagerLoadingInvocationHandler.eagerLoad(
                    new StubInvocationHandler(), 
                    null,
                    EagerLoadingProxyInterface.class
                )
            );
        }

        @Test
        @DisplayName("should throw when proxy interface argument is null")
        void test5() {
            assertThrows(
                IllegalArgumentException.class,
                () -> EagerLoadingInvocationHandler.eagerLoad(
                    new StubInvocationHandler(), 
                    new StubCacheStrategy<>(),
                    null
                )
            );
        }

        @Test
        @DisplayName(
            "should eager load properties whose proxy interface methods " +
            "are annotated with @ExternalizedProperty and have no arguments"
        )
        void test6() {
            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            // Eager loads properties.
            Class<EagerLoadingProxyInterface> proxyInterface = 
                EagerLoadingProxyInterface.class;
            
            EagerLoadingInvocationHandler.eagerLoad(
                new StubInvocationHandler(),
                cacheStrategy,
                proxyInterface
            );

            for (Method method : proxyInterface.getDeclaredMethods()) {
                ExternalizedProperty externalizedProperty =
                    method.getAnnotation(ExternalizedProperty.class);
                if (externalizedProperty != null) {
                    Optional<Object> cachedValue = cacheStrategy.get(method);
                    assertTrue(cachedValue.isPresent());
                }
            }
        }

        @Test
        @DisplayName(
            "should eager load properties whose proxy interface methods " +
            "are not annotated with @ExternalizedProperty but are default interface methods"
        )
        void test7() {
            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            Class<EagerLoadingProxyInterface> proxyInterface = 
                EagerLoadingProxyInterface.class;
            
            EagerLoadingInvocationHandler.eagerLoad(
                new StubInvocationHandler(),
                cacheStrategy,
                proxyInterface
            );

            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                EagerLoadingProxyInterface::propertyWithNoAnnotationButWithDefaultValue
            );
            Optional<Object> cachedValue = cacheStrategy.get(method);
            assertTrue(cachedValue.isPresent());
        }

        @Test
        @DisplayName(
            "should not eager load properties whose proxy interface methods " + 
            "have no @ExternalizedProperty annotation"
        )
        void test8() {
            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            Class<NoEagerLoadingProxyInterface> proxyInterface =
                NoEagerLoadingProxyInterface.class;

            // Eager loads properties.
            EagerLoadingInvocationHandler.eagerLoad(
                new StubInvocationHandler(),
                cacheStrategy,
                proxyInterface
            );

            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                NoEagerLoadingProxyInterface::noAnnotation
            );

            Optional<Object> cachedValue = cacheStrategy.get(method);
            assertFalse(cachedValue.isPresent());
        }

        @Test
        @DisplayName(
            "should not eager load properties whose proxy interface methods have arguments"
        )
        void test9() {
            CacheStrategy<Method, Object> cacheStrategy = 
                new StubCacheStrategy<>();

            Class<NoEagerLoadingProxyInterface> proxyInterface = 
                NoEagerLoadingProxyInterface.class;
            
            // Eager loads properties.
            EagerLoadingInvocationHandler.eagerLoad(
                new StubInvocationHandler(),
                cacheStrategy,
                proxyInterface
            );

            Method method = ProxyMethodUtils.getMethod(
                proxyInterface, 
                NoEagerLoadingProxyInterface::withParameters
            );

            Optional<Object> cachedValue = cacheStrategy.get(method);
            assertFalse(cachedValue.isPresent());
        }

        @Test
        @DisplayName(
            "should thrown when decorated invocation handler throws during eager loading"
        )
        void test10() throws Throwable {
            Class<EagerLoadingProxyInterface> proxyInterface = 
                EagerLoadingProxyInterface.class;

            // Always return null.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.THROWING_HANDLER
            );

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();
            
            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> EagerLoadingInvocationHandler.eagerLoad(
                    decorated,
                    cacheStrategy,
                    proxyInterface
                )
            );
        }
    }

    @Nested
    class InvokeMethod {
        @Test
        @DisplayName("should return eagerly loaded property values")
        void test1() throws Throwable {
            Class<EagerLoadingProxyInterface> proxyInterface = 
                EagerLoadingProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                EagerLoadingProxyInterface::javaVersion
            );

            StubCacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();
            // Add to cache.
            cacheStrategy.cache(proxyMethod, "cached-value");

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            // Eager loads properties.
            EagerLoadingInvocationHandler invocationHandler = 
                EagerLoadingInvocationHandler.eagerLoad(
                    decorated, 
                    cacheStrategy,
                    proxyInterface
                );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
                proxyMethod, 
                null
            );

            assertNotNull(result);
            assertEquals("cached-value", result);
            assertSame(
                cacheStrategy.getCache().get(proxyMethod), 
                result
            );
        }

        @Test
        @DisplayName(
            "should resolve properties that were not eagerly loaded " + 
            "from decorated invocation handler and cache it"
        )
        void test2() throws Throwable {
            Class<NoEagerLoadingProxyInterface> proxyInterface = 
                NoEagerLoadingProxyInterface.class;

            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                // This property method will not be eagerly loaded
                // because it has a parameter.
                NoEagerLoadingProxyInterface::withParameters
            );

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            EagerLoadingInvocationHandler invocationHandler = 
                EagerLoadingInvocationHandler.eagerLoad(
                    decorated,
                    cacheStrategy,
                    proxyInterface
                );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
                proxyMethod, 
                new Object[] { "will not eager load" }
            );

            assertNotNull(result);
            // Resolved from decorated invocation handler.
            assertEquals("value-from-decorated-handler", result);
            // Resolved value was cached.
            assertTrue(cacheStrategy.get(proxyMethod).isPresent());
        }

        @Test
        @DisplayName(
            "should return null " + 
            "when property could not be resolved from decorated invocation handler"
        )
        void test3() throws Throwable {
            Class<EagerLoadingProxyInterface> proxyInterface = 
                EagerLoadingProxyInterface.class;
            
            Method proxyMethod = ProxyMethodUtils.getMethod(
                proxyInterface,
                // This property method will not be eagerly loaded
                // because it has a parameter.
                EagerLoadingProxyInterface::javaVersion
            );

            // Always return null.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.NULL_HANDLER
            );

            // No cached results.
            CacheStrategy<Method, Object> cacheStrategy = new StubCacheStrategy<>();

            EagerLoadingInvocationHandler invocationHandler = 
                EagerLoadingInvocationHandler.eagerLoad(
                    decorated,
                    cacheStrategy,
                    proxyInterface
                );
            
            Object result = invocationHandler.invoke(
                stubProxy(proxyInterface, decorated), 
                proxyMethod, 
                null
            );

            assertNull(result);
            // Not cached.
            assertFalse(cacheStrategy.get(proxyMethod).isPresent());
        }

        @SuppressWarnings("unchecked")
        private <T> T stubProxy(Class<T> proxyInterface, InvocationHandler decorated) {
            return (T)Proxy.newProxyInstance(
                proxyInterface.getClassLoader(), 
                new Class<?>[] { proxyInterface }, 
                decorated
            );
        }
    }

    static interface EagerLoadingProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();

        // No annotation with constant default value.
        default String propertyWithNoAnnotationButWithDefaultValue() {
            return "default.value";
        }
    }

    static interface NoEagerLoadingProxyInterface {
        String noAnnotation();
    
        @ExternalizedProperty("with.parameters")
        String withParameters(String willNotEagerLoad);
    }
}
