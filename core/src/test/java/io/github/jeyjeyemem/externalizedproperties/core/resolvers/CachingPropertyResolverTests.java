package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.CacheStrategy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
                () -> new CachingPropertyResolver(null, Duration.ofMinutes(5), expiryScheduler)
            );
        }

        @Test
        @DisplayName("should throw when cache item lifetime argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(new SystemPropertyResolver(), null, expiryScheduler)
            );
        }

        @Test
        @DisplayName("should throw when expiry scheduler argument is null")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingPropertyResolver(
                    new SystemPropertyResolver(), 
                    Duration.ofMinutes(5), 
                    null
                )
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
                    expiryScheduler,
                    null
                )
            );
        }
    }

    @Nested
    class ResolveMethod {
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
        @DisplayName("should resolve values from the decorated resolver")
        public void test1() {
            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );

            ExternalizedPropertyResolverResult result = resolver.resolve("java.version");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findResolvedProperty("java.version")
                    .map(ResolvedProperty::value)
                    .orElse(null)
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

            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.property");
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property"));
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

            ExternalizedPropertyResolverResult result = resolver.resolve("java.version");
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            // Check if property was cached via strategy.
            assertEquals(
                System.getProperty("java.version"), 
                cacheStrategy.getCache().get("java.version").value()
            );

            assertEquals(
                System.getProperty("java.version"), 
                result.findResolvedProperty("java.version")
                    .map(ResolvedProperty::value)
                    .orElse(null)
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

            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.property");
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property"));

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("nonexisting.property"));
        }

        @Test
        @DisplayName("should return cached properties")
        public void cacheTest3() {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();
            // Cached value.
            cacheStrategy.cache(ResolvedProperty.with("property.cached", "cached.value"));

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofSeconds(30),
                cacheStrategy
            );

            // property.cache is not in system properties but is in the strategy cache.
            ExternalizedPropertyResolverResult result = resolver.resolve("property.cached");
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());
            
            assertEquals(
                cacheStrategy.getCache().get("property.cached").value(), 
                result.findResolvedProperty("property.cached")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName("should removed cache properties after cache item lifetime expires")
        public void cacheTest4() throws InterruptedException {
            StubCacheStrategy cacheStrategy = new StubCacheStrategy();

            CachingPropertyResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                Duration.ofMillis(500),
                cacheStrategy
            );

            ExternalizedPropertyResolverResult result = resolver.resolve("java.version");

            // Check if property was cached via strategy.
            assertEquals(
                result.findResolvedProperty("java.version")
                    .map(ResolvedProperty::value)
                    .orElse(null), 
                cacheStrategy.getCache().get("java.version").value()
            );
            
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findResolvedProperty("java.version")
                    .map(ResolvedProperty::value)
                    .orElse(null)
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
                    new CachingPropertyResolver.ConcurrentMapCacheStrategy(cache);

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
                    new CachingPropertyResolver.ConcurrentMapCacheStrategy(cache);

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
                    new CachingPropertyResolver.ConcurrentMapCacheStrategy(cache);

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
            return new CachingPropertyResolver(decorated, cacheItemLifetime, expiryScheduler);
        }

        return new CachingPropertyResolver(
            decorated, 
            cacheItemLifetime, 
            expiryScheduler,
            cacheStrategy
        );
    }

    private static class StubCacheStrategy implements CacheStrategy {
        private final Map<String, ResolvedProperty> cache = new HashMap<>();
        private final CountDownLatch latch = new CountDownLatch(1);

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
            latch.countDown();
        }

        public Map<String, ResolvedProperty> getCache() {
            return Collections.unmodifiableMap(cache);
        }

        public boolean waitForExpiry() throws InterruptedException {
            return latch.await(10, TimeUnit.SECONDS);
        }

    }
}
