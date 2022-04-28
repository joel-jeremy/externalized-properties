package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethods;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.StubCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.StubResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CachingResolverTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingResolver(null, new StubCacheStrategy<>())
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null.")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingResolver(
                    new StubResolver(),
                    null
                )
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should throw when decorated argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CachingResolver.provider(null, new StubCacheStrategy<>())
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> CachingResolver.provider(StubResolver.provider(), null)
            );
        }

        @Test
        @DisplayName("should not return null.")
        public void test3() {
            ResolverProvider<CachingResolver> provider = 
                CachingResolver.provider(StubResolver.provider(), new StubCacheStrategy<>());

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get")
        public void test4() {
            ResolverProvider<CachingResolver> provider = 
                CachingResolver.provider(StubResolver.provider(), new StubCacheStrategy<>());

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null.")
        // void validationTest1() {
        //     CachingResolver resolver = resolverToTest(
        //         new StubResolver()
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(
        //             null,
        //             "property"
        //         )
        //     );
        // }

        // @Test
        // @DisplayName("should throw when property name argument is null or empty.")
        // void validationTest2() {
        //     CachingResolver resolver = resolverToTest(
        //         new StubResolver()
        //     );
        //     ProxyMethod proxyMethod = proxyMethod(resolver);

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(proxyMethod, (String)null)
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> resolver.resolve(proxyMethod, "")
        //     );
        // }

        @Test
        @DisplayName("should resolve property value from the decorated resolver.")
        void test1() {
            String propertyName = "property";
            StubResolver decorated = new StubResolver();

            CachingResolver resolver = resolverToTest(decorated);

            ProxyMethod proxyMethod = ProxyMethods.property();
            Optional<String> result = resolver.resolve(proxyMethod, propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                decorated.resolvedProperties().get(propertyName), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the decorated resolver."
        )
        void test2() {
            CachingResolver resolver = resolverToTest(
                new SystemPropertyResolver()
            );
            ProxyMethod proxyMethod = ProxyMethods.property();
            // Not in system properties.
            Optional<String> result = resolver.resolve(proxyMethod, "property");
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should cache resolved property.")
        void cacheTest1() {
            String propertyName = "property";
            StubResolver decorated = new StubResolver();
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            ProxyMethod proxyMethod = ProxyMethods.property();
            Optional<String> result = resolver.resolve(proxyMethod, propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());

            // Check if property was cached via strategy.
            assertEquals(
                result.get(),
                cacheStrategy.getCache().get(propertyName)
            );
        }

        @Test
        @DisplayName("should not cache unresolved property.")
        void cacheTest2() {
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            CachingResolver resolver = resolverToTest(
                new SystemPropertyResolver(),
                cacheStrategy
            );

            ProxyMethod proxyMethod = ProxyMethods.property();
            // Property is not in system properties.
            Optional<String> result = resolver.resolve(
                proxyMethod,
                "property"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("property"));
        }

        @Test
        @DisplayName("should return cached property.")
        void cacheTest3() {
            String propertyName = "property";
            StubResolver decorated = new StubResolver();

            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();
            // Cache values.
            cacheStrategy.cache(propertyName, "cached.value");

            CachingResolver resolver = resolverToTest(
                decorated,
                cacheStrategy
            );

            ProxyMethod proxyMethod = ProxyMethods.property();
            // property.cache is not in system properties but is in the strategy cache.
            Optional<String> result = resolver.resolve(proxyMethod, propertyName);
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            // Did not go through decorated resolver.
            assertFalse(decorated.resolvedPropertyNames().contains(propertyName));
            // Got property from cache..
            assertEquals(
                cacheStrategy.getCache().get(propertyName), 
                result.get()
            );
        }
    }

    private CachingResolver resolverToTest(Resolver decorated) {
        return resolverToTest(
            decorated,
            new StubCacheStrategy<>()
        );
    }

    private CachingResolver resolverToTest(
            Resolver decorated,
            CacheStrategy<String, String> cacheStrategy
    ) {
        return new CachingResolver(
            decorated, 
            cacheStrategy
        );
    }
}
