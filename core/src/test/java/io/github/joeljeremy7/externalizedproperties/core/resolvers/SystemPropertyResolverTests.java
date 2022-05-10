package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemPropertyResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        void test1() {
            ResolverProvider<SystemPropertyResolver> provider = 
                SystemPropertyResolver.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        void test2() {
            ResolverProvider<SystemPropertyResolver> provider = 
                SystemPropertyResolver.provider();

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from system properties.")
        void test1() {
            SystemPropertyResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::javaVersion
            );

            Optional<String> result = resolver.resolve(proxyMethod, "java.version");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getProperty("java.version"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when system property is not found."
        )
        void test2() {
            SystemPropertyResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notFound
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property" // Not in system properties.
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    private SystemPropertyResolver resolverToTest() {
        return new SystemPropertyResolver();
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();

        @ExternalizedProperty("not.found")
        String notFound();
    }
}
