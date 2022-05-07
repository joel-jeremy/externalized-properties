package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubCacheStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CachingExternalizedPropertiesTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when decorated externalized properties argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingExternalizedProperties(
                    null,
                    new StubCacheStrategy<>()
                )
            );
        }

        @Test
        @DisplayName("should throw when resolved property cache strategy argument is null")
        public void test2() {
            ExternalizedProperties decorated = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new CachingExternalizedProperties(
                    decorated,
                    null
                )
            );
        }
    }

    @Nested
    class ProxyMethod {
        @Test
        @DisplayName("should delegate to decorated externalized properties")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>()
                );

            ProxyInterface proxy = cachingExternalizedProperties.proxy(
                ProxyInterface.class
            );

            assertNotNull(proxy);
        }

        @Test
        @DisplayName(
            "should cache returned proxy instance from decorated externalized properties"
        )
        public void test2() {
            ExternalizedProperties decorated = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>()
                );

            ProxyInterface proxy1 = cachingExternalizedProperties.proxy(
                ProxyInterface.class
            );
            ProxyInterface proxy2 = cachingExternalizedProperties.proxy(
                ProxyInterface.class
            );

            assertSame(proxy1, proxy2);
        }
    }

    @Nested
    class ProxyMethodWithClassLoaderOverload {
        @Test
        @DisplayName("should delegate to decorated externalized properties")
        public void test1() {
            ExternalizedProperties decorated = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>()
                );

            ProxyInterface proxy = cachingExternalizedProperties.proxy(
                ProxyInterface.class,
                ProxyInterface.class.getClassLoader()
            );

            assertNotNull(proxy);
        }

        @Test
        @DisplayName(
            "should cache returned proxy instance from decorated externalized properties"
        )
        public void test2() {
            ExternalizedProperties decorated = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            // Empty cache.
            CachingExternalizedProperties cachingExternalizedProperties = 
                new CachingExternalizedProperties(
                    decorated,
                    new StubCacheStrategy<>()
                );

            ProxyInterface proxy1 = cachingExternalizedProperties.proxy(
                ProxyInterface.class,
                ProxyInterface.class.getClassLoader()
            );
            
            ProxyInterface proxy2 = cachingExternalizedProperties.proxy(
                ProxyInterface.class,
                ProxyInterface.class.getClassLoader()
            );

            assertSame(proxy1, proxy2);
        }
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();
    }
}
