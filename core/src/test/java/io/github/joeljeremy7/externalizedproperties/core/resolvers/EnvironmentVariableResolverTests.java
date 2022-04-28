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

public class EnvironmentVariableResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<EnvironmentVariableResolver> provider = 
                EnvironmentVariableResolver.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<EnvironmentVariableResolver> provider = 
                EnvironmentVariableResolver.provider();

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }
    
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from environment variables.")
        void test1() {
            EnvironmentVariableResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::path
            );

            Optional<String> result = resolver.resolve(
                proxyMethod,
                "PATH"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getenv("PATH"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when environment variable is not found."
        )
        void test2() {
            EnvironmentVariableResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notFound
            );

            Optional<String> result = resolver.resolve(
                proxyMethod,
                "not.found"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should attempt to resolve environment variable by formatting " + 
            "property name to environment variable format."
        )
        void test3() {
            EnvironmentVariableResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::javaHome
            );

            Optional<String> result1 = resolver.resolve(
                proxyMethod,
                // java.home should be converted to JAVA_HOME
                "java.home"
            );

            Optional<String> result2 = resolver.resolve(
                proxyMethod,
                // java-home should be converted to JAVA_HOME
                "java-home"  
            );

            assertNotNull(result1);
            assertNotNull(result2);
            assertTrue(result1.isPresent());
            assertTrue(result2.isPresent());
            assertEquals(System.getenv("JAVA_HOME"), result1.get());
            assertEquals(System.getenv("JAVA_HOME"), result2.get());
        }
    }

    private EnvironmentVariableResolver resolverToTest() {
        return new EnvironmentVariableResolver();
    }

    public static interface ProxyInterface {
        /**
         * EnvironmentVariableResolver supports formatting of
         * property names such that path is converted to PATH.
         */
        @ExternalizedProperty("path")
        String path();

        /**
         * EnvironmentVariableResolver supports formatting of
         * property names such that java.home is converted to JAVA_HOME.
         */
        @ExternalizedProperty("java.home")
        String javaHome();

        @ExternalizedProperty("not.found")
        String notFound();
    }
}
