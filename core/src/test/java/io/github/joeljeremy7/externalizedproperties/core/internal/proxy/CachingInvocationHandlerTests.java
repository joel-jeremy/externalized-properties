package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.CachingInvocationHandler.InvocationCacheKey;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
        void test1() {
            StubCacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingInvocationHandler(
                    null,
                    cacheStrategy
                )
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        void test2() {
            StubInvocationHandler decorated = new StubInvocationHandler();
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingInvocationHandler(
                    decorated,
                    null
                )
            );
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
                // null because proxies give null instead of empty array
                // when there are no arguments...
                null
            );

            assertNotNull(result);
            assertEquals("cached-value", result);
            assertSame(
                cacheStrategy.getCache().get(new InvocationCacheKey(stubMethod)), 
                result
            );
        }

        @Test
        @DisplayName(
            "should resolve uncached property values from decorated invocation handler" +
            "and cache the resolved value"
        )
        void test2() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                i -> "value-from-decorated-handler"
            );

            // No cached results.
            CacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    cacheStrategy
                );
            
            Object result = cachingInvocationHandler.invoke(
                stubProxy(decorated), 
                stubMethod, 
                // null because proxies give null instead of empty array
                // when there are no arguments...
                null
            );

            assertNotNull(result);
            // Resolved from decorated invocation handler.
            assertEquals("value-from-decorated-handler", result);
            // Resolved value was cached.
            assertTrue(cacheStrategy.get(new InvocationCacheKey(stubMethod)).isPresent());
        }

        @Test
        @DisplayName(
            "should not cache and return null " + 
            "when property could not be resolved from decorated invocation handler"
        )
        void test3() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return null.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.NULL_HANDLER
            );

            // No cached results.
            CacheStrategy<InvocationCacheKey, Object> cacheStrategy = new StubCacheStrategy<>();

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    cacheStrategy
                );
            
            Object result = cachingInvocationHandler.invoke(
                stubProxy(decorated), 
                stubMethod, 
                // null because proxies give null instead of empty array
                // when there are no arguments...
                null
            );

            assertNull(result);
            // Not cached.
            assertFalse(cacheStrategy.get(new InvocationCacheKey(stubMethod)).isPresent());
        }
        @Test
        @DisplayName("should throw when decorated invocation handler throws")
        void test4() throws Throwable {
            Method stubMethod = stubMethod();

            // Always return the same string for any invoked proxy method.
            StubInvocationHandler decorated = new StubInvocationHandler(
                StubInvocationHandler.THROWING_HANDLER
            );

            CachingInvocationHandler cachingInvocationHandler = 
                new CachingInvocationHandler(
                    decorated,
                    new StubCacheStrategy<>()
                );
            
            Object stubProxy = stubProxy(decorated);
            
            assertThrows(
            ExternalizedPropertiesException.class, 
                () ->cachingInvocationHandler.invoke(
                    stubProxy, 
                    stubMethod, 
                    // null because proxies give null instead of empty array
                    // when there are no arguments...
                    null
                )
            );
        }
    }

    @Nested
    class InvocationCacheKeyTests {
        @Nested
        class HashCodeMethod {
            @Test
            @DisplayName("should return the same of hash code everytime")
            void test1() {
                Method method = stubMethod();
                Object[] args = new Object[] { "1", Class.class };
                InvocationCacheKey cacheKey = new InvocationCacheKey(method, args);
                int hashCode1 = cacheKey.hashCode();
                int hashCode2 = cacheKey.hashCode();

                assertEquals(hashCode1, hashCode2);
            }

            @Test
            @DisplayName(
                "should return the same of hash code for matching methods " + 
                "and args combinations"
            )
            void test2() {
                Method method1 = stubMethod();
                Method method2 = stubMethod();
                Object[] args1 = new Object[] { "1", Class.class };
                Object[] args2 = new Object[] { "1", Class.class };
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);
                assertEquals(cacheKey1.hashCode(), cacheKey2.hashCode());
            }

            @Test
            @DisplayName(
                "should return the same of hash code for matching methods " + 
                "(no args)"
            )
            void test3() {
                Method method1 = stubMethod();
                Method method2 = stubMethod();
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2);
                assertEquals(cacheKey1.hashCode(), cacheKey2.hashCode());
            }

            @Test
            @DisplayName(
                "should return a different hash code for different args combinations"
            )
            void test4() {
                Method method = stubMethod();
                Object[] args1 = new Object[] { "1", Class.class };
                Object[] args2 = new Object[] { "2", Class.class };
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method, args1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method, args2);
                int hashCode1 = cacheKey1.hashCode();
                int hashCode2 = cacheKey2.hashCode();

                assertNotEquals(hashCode1, hashCode2);
            }
        }

        @Nested
        class EqualsMethod {
            @Test
            @DisplayName("should return true when method and args are all equal")
            void test1() {
                Method method1 = stubMethod();
                Method method2 = stubMethod();
                Object[] args1 = new Object[] { "1", Class.class };
                Object[] args2 = new Object[] { "1", Class.class };
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

                assertTrue(cacheKey1.equals(cacheKey2));
            }

            @Test
            @DisplayName(
                "should return true when method and args are all equal " + 
                "(args has an array)"
            )
            void test2() {
                Method method1 = stubMethod();
                Method method2 = stubMethod();
                Object[] args1 = new Object[] { 
                    "1", 
                    new Object[] { Class.class, Integer.class, InvocationCacheKey.class }
                };
                Object[] args2 = new Object[] { 
                    "1", 
                    new Object[] { Class.class, Integer.class, InvocationCacheKey.class }
                };
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

                assertTrue(cacheKey1.equals(cacheKey2));
            }

            @Test
            @DisplayName("should return false when methods are not equal (no args)")
            void test3() {
                Method method = ProxyMethodUtils.getMethod(
                    StubProxyInterface.class, 
                    StubProxyInterface::methodName
                );
                Method otherMethod = ProxyMethodUtils.getMethod(
                    OtherProxyInterface.class, 
                    OtherProxyInterface::methodName
                );
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(otherMethod);

                assertFalse(cacheKey1.equals(cacheKey2));
            }

            @Test
            @DisplayName("should return false args are not all equal")
            void test4() {
                Method method1 = stubMethod();
                Method method2 = stubMethod();
                Object[] args1 = new Object[] { 
                    "1", 
                    new Object[] { Class.class, Integer.class, String.class }
                };
                Object[] args2 = new Object[] { 
                    "1", 
                    new Object[] { Class.class, Integer.class, InvocationCacheKey.class }
                };
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method1, args1);
                InvocationCacheKey cacheKey2 = new InvocationCacheKey(method2, args2);

                assertFalse(cacheKey1.equals(cacheKey2));
            }

            @Test
            @DisplayName(
                "should return false when other object is not an invocation cache key"
            )
            void test5() {
                Method method = ProxyMethodUtils.getMethod(
                    StubProxyInterface.class, 
                    StubProxyInterface::methodName
                );
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);

                assertFalse(cacheKey1.equals(new Object()));
            }

            @Test
            @DisplayName("should return false when other object is null")
            void test6() {
                Method method = ProxyMethodUtils.getMethod(
                    StubProxyInterface.class, 
                    StubProxyInterface::methodName
                );
                InvocationCacheKey cacheKey1 = new InvocationCacheKey(method);

                assertFalse(cacheKey1.equals(null));
            }
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

    static interface OtherProxyInterface {
        String methodName();
    }
}
