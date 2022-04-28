package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
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

            BasicProxyInterface proxy = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class
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

            BasicProxyInterface proxy1 = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class
            );
            BasicProxyInterface proxy2 = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class
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

            BasicProxyInterface proxy = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
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

            BasicProxyInterface proxy1 = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
            );
            
            BasicProxyInterface proxy2 = cachingExternalizedProperties.proxy(
                BasicProxyInterface.class,
                BasicProxyInterface.class.getClassLoader()
            );

            assertSame(proxy1, proxy2);
        }
    }
}
