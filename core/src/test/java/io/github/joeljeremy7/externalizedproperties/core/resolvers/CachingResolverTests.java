package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
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
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated argument is null.")
        void test1() {
            StubCacheStrategy<String, String> cacheStrategy = new StubCacheStrategy<>();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingResolver(null, cacheStrategy)
            );
        }

        @Test
        @DisplayName("should throw when cache strategy argument is null.")
        void test2() {
            StubResolver decorated = new StubResolver();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingResolver(decorated, null)
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

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
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
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            // Not in system properties.
            Optional<String> result = resolver.resolve(proxyMethod, "non.existent");
            
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

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
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

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
            // Property is not in system properties.
            Optional<String> result = resolver.resolve(
                proxyMethod,
                "not.found"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());

            // Check if property was cached via strategy.
            assertFalse(cacheStrategy.getCache().containsKey("not.found"));
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

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolver)
            );
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

    private static CachingResolver resolverToTest(Resolver decorated) {
        return resolverToTest(
            decorated,
            new StubCacheStrategy<>()
        );
    }

    private static CachingResolver resolverToTest(
            Resolver decorated,
            CacheStrategy<String, String> cacheStrategy
    ) {
        return new CachingResolver(
            decorated, 
            cacheStrategy
        );
    }
    
    private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
        return ExternalizedProperties.builder().resolvers(resolvers).build();
    }

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
