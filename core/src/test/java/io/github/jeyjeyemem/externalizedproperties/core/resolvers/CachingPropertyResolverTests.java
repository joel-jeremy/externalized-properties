package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.ConcurrentMapCacheStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachingPropertyResolverTests {
    private static final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor();

    @AfterAll
    public static void cleanup() {
        expiryScheduler.shutdown();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(null, Duration.ofMinutes(5))
            );
        }

        @Test
        @DisplayName("should throw when cache item lifetime argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(new SystemPropertyResolver(), null)
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null")
        public void test4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(
                    new SystemPropertyResolver(),
                    Duration.ofMinutes(5),
                    null
                )
            );
        }
    }

    @Nested
    class ResolveMethodSingleProperty {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void validationTest1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String)null)
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve("")
            );
        }

        @Test
        @DisplayName("should resolve property value from the decorated resolver")
        public void test1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            Optional<ResolvedProperty> result = resolver.resolve("java.version");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getProperty("java.version"), 
                result.get().value()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the decorated resolver"
        )
        public void test2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            Optional<ResolvedProperty> result = resolver.resolve("nonexistent.property");
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should cache resolved property")
        public void cacheTest1() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            Optional<ResolvedProperty> result = resolver.resolve("java.version");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getProperty("java.version"), 
                result.get().value()
            );

            // Check if property was cached via strategy.
            assertEquals(
                System.getProperty("java.version"), 
                cacheStrategy.getCache().get("java.version").value()
            );
        }

        @Test
        @DisplayName("should not cache unresolved property")
        public void cacheTest2() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            Optional<ResolvedProperty> result = resolver.resolve(
                "nonexistent.property"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("nonexistent.property"));
        }

        @Test
        @DisplayName("should return cached property")
        public void cacheTest3() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();
            // Cached values.
            cacheStrategy.cache(ResolvedProperty.with("property.cached", "cached.value"));

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            // property.cache is not in system properties but is in the strategy cache.
            Optional<ResolvedProperty> result = resolver.resolve("property.cached");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                cacheStrategy.getCache().get("property.cached").value(), 
                result.get().value()
            );
        }

        @Test
        @DisplayName("should remove cached property after cache item lifetime expires")
        public void cacheTest4() throws InterruptedException {
            // 1 because we need to wait for 1 cache entry to expire.
            CountDownLatch expiryLatch = new CountDownLatch(1);
            StubCacheStrategy cacheStrategy = new StubCacheStrategy(expiryLatch);

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofMillis(500),
                cacheStrategy
            );

            Optional<ResolvedProperty> result = resolver.resolve("java.version");

            assertNotNull(result);
            assertTrue(result.isPresent());

            // Check if property was cached via strategy.
            assertEquals(
                result.get().value(), 
                cacheStrategy.getCache().get("java.version").value()
            );

            assertEquals(
                System.getProperty("java.version"), 
                result.get().value()
            );

            // Block until cache item lifetime elapses and 
            // StubCacheStrategy.expire() gets called.
            assertTrue(cacheStrategy.waitForExpiry());

            assertFalse(cacheStrategy.getCache().containsKey("java.version"));
        }

        @Nested
        class DefaultMapCacheStrategyTests {
            @Test
            @DisplayName("should cache resolved property to the cache map")
            public void cacheMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                cacheStrategy.cache(property);

                assertEquals(
                    property, 
                    cache.get(property.name())
                );
            }

            @Test
            @DisplayName("should return cached resolved property from the cache map")
            public void getFromCacheMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                cache.put(property.name(), property);

                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                Optional<ResolvedProperty> cachedProperty = 
                    cacheStrategy.getFromCache(property.name());

                assertTrue(cachedProperty.isPresent());
                assertSame(
                    property, 
                    cachedProperty.get()
                );
            }

            @Test
            @DisplayName("should expire cached resolved property from the cache map")
            public void expireMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                cache.put(property.name(), property);

                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                cacheStrategy.expire(property);

                // Delted from cache map.
                assertFalse(cache.containsKey(property.name()));
            }
        }
    }

    @Nested
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty")
        public void validationTest1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((Collection<String>)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when property names varargs argument is null or empty")
        public void validationTest2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String[])null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[0])
            );
        }

        @Test
        @DisplayName(
            "should throw when property names collection contains any null or empty values"
        )
        public void validationTest3() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", null))
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", ""))
            );
        }

        @Test
        @DisplayName("should throw when property names varargs contain any null or empty values")
        public void validationTest4() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", null })
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", "" })
            );
        }

        @Test
        @DisplayName("should resolve property values from the decorated resolver")
        public void test1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            ExternalizedPropertyResolverResult result = resolver.resolve(
                "java.version",
                "java.home"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredPropertyValue("java.version")
            );

            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredPropertyValue("java.home")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the decorated resolver"
        )
        public void test2() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            ExternalizedPropertyResolverResult result = resolver.resolve(
                "nonexisting.property1",
                "nonexisting.property2"
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property1"));
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property2"));
        }

        @Test
        @DisplayName("should cache resolved properties")
        public void cacheTest1() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            ExternalizedPropertyResolverResult result = resolver.resolve(
                "java.version",
                "java.home"
            );
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // Check if property was cached via strategy.
            assertEquals(
                System.getProperty("java.version"), 
                cacheStrategy.getCache().get("java.version").value()
            );

            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredPropertyValue("java.version")
            );

            // Check if property was cached via strategy.
            assertEquals(
                System.getProperty("java.home"), 
                cacheStrategy.getCache().get("java.home").value()
            );

            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredPropertyValue("java.home")
            );
        }

        @Test
        @DisplayName("should not cache unresolved properties")
        public void cacheTest2() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            ExternalizedPropertyResolverResult result = resolver.resolve(
                "nonexisting.property1",
                "nonexisting.property2"
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property1"));
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property2"));

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("nonexisting.property1"));
            assertFalse(cacheStrategy.getCache().containsKey("nonexisting.property2"));
        }

        @Test
        @DisplayName("should return cached properties")
        public void cacheTest3() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();
            // Cached values.
            cacheStrategy.cache(ResolvedProperty.with("property.cached1", "cached.value1"));
            cacheStrategy.cache(ResolvedProperty.with("property.cached2", "cached.value2"));

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            // property.cache is not in system properties but is in the strategy cache.
            ExternalizedPropertyResolverResult result = resolver.resolve(
                "property.cached1",
                "property.cached2"
            );
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            
            assertEquals(
                cacheStrategy.getCache().get("property.cached1").value(), 
                result.findRequiredPropertyValue("property.cached1")
            );

            assertEquals(
                cacheStrategy.getCache().get("property.cached2").value(), 
                result.findRequiredPropertyValue("property.cached2")
            );
        }

        @Test
        @DisplayName("should remove cached properties after cache item lifetime expires")
        public void cacheTest4() throws InterruptedException {
            // 2 because we need to wait for 2 cache entries to expire.
            CountDownLatch expiryLatch = new CountDownLatch(2);
            StubCacheStrategy cacheStrategy = new StubCacheStrategy(expiryLatch);

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofMillis(500),
                cacheStrategy
            );

            ExternalizedPropertyResolverResult result = resolver.resolve(
                "java.version",
                "java.home"
            );

            // Check if property was cached via strategy.
            assertEquals(
                result.findRequiredPropertyValue("java.version"),
                cacheStrategy.getCache().get("java.version").value()
            );

            // Check if property was cached via strategy.
            assertEquals(
                result.findRequiredPropertyValue("java.home"),
                cacheStrategy.getCache().get("java.home").value()
            );
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredPropertyValue("java.version")
            );

            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredPropertyValue("java.home")
            );

            // Block until cache item lifetime elapses and 
            // StubCacheStrategy.expire() gets called.
            assertTrue(cacheStrategy.waitForExpiry());

            assertFalse(cacheStrategy.getCache().containsKey("java.version"));
            assertFalse(cacheStrategy.getCache().containsKey("java.home"));
        }

        @Nested
        class DefaultMapCacheStrategyTests {
            @Test
            @DisplayName("should cache resolved property to the cache map")
            public void cacheMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                cacheStrategy.cache(property);

                assertEquals(
                    property, 
                    cache.get(property.name())
                );
            }

            @Test
            @DisplayName("should return cached resolved property from the cache map")
            public void getFromCacheMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                cache.put(property.name(), property);

                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                Optional<ResolvedProperty> cachedProperty = 
                    cacheStrategy.getFromCache(property.name());

                assertTrue(cachedProperty.isPresent());
                assertSame(
                    property, 
                    cachedProperty.get()
                );
            }

            @Test
            @DisplayName("should expire cached resolved property from the cache map")
            public void expireMethod() {
                ResolvedProperty property = ResolvedProperty.with(
                    "property.name", 
                    "property.value"
                );

                ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
                cache.put(property.name(), property);

                CacheStrategy cacheStrategy = 
                    new ConcurrentMapCacheStrategy(cache);

                cacheStrategy.expire(property);

                // Delted from cache map.
                assertFalse(cache.containsKey(property.name()));
            }
        }
    }

    private CachingPropertyResolver resolverToTest(
            ExternalizedPropertyResolver decorated
    ) {
        return resolverToTest(
            decorated,
            Duration.ofSeconds(30),
            null
        );
    }

    private CachingPropertyResolver resolverToTest(
            ExternalizedPropertyResolver decorated,
            Duration cacheItemLifetime,
            CacheStrategy cacheStrategy
    ) {
        if (cacheStrategy == null) {
            return new CachingPropertyResolver(decorated, cacheItemLifetime);
        }

        return new CachingPropertyResolver(
            decorated, 
            cacheItemLifetime,
            cacheStrategy
        );
    }

    private static class StubCacheStrategy implements CacheStrategy {
        private final Map<String, ResolvedProperty> cache = new HashMap<>();
        private final CountDownLatch expiryLatch;

        public StubCacheStrategy() {
            this.expiryLatch = new CountDownLatch(1);
        }

        public StubCacheStrategy(CountDownLatch expiryLatch) {
            this.expiryLatch = expiryLatch;
        }

        @Override
        public void cache(ResolvedProperty resolvedProperty) {
            cache.put(resolvedProperty.name(), resolvedProperty);
        }

        @Override
        public Optional<ResolvedProperty> getFromCache(String propertyName) {
            return Optional.ofNullable(cache.get(propertyName));
        }

        @Override
        public void expire(ResolvedProperty resolvedProperty) {
            cache.remove(resolvedProperty.name());
            // Release latch if anything is waiting for it.
            expiryLatch.countDown();
        }

        public Map<String, ResolvedProperty> getCache() {
            return Collections.unmodifiableMap(cache);
        }

        public boolean waitForExpiry() throws InterruptedException {
            return expiryLatch.await(10, TimeUnit.SECONDS);
        }

    }
}
