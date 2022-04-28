package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.ProxyMethods;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultResolverTests {
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<DefaultResolver> provider = 
                DefaultResolver.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<DefaultResolver> provider = 
                DefaultResolver.provider();

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from system properties.")
        void systemPropertyTest1() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = ProxyMethods.javaVersion();

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
        void systemPropertyTest2() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = ProxyMethods.property();

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should resolve property value from environment variables.")
        void environmentVariableTest1() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = ProxyMethods.path();

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
        void environmentVariableTest2() {
            DefaultResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = ProxyMethods.property();

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    private DefaultResolver resolverToTest() {
        return new DefaultResolver();
    }
}
