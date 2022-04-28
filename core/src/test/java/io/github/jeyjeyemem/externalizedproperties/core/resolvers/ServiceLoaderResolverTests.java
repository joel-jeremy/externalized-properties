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

/**
 * Service loader resolvers are configured in resources/META-INF/services folder.
 */
public class ServiceLoaderResolverTests {
    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should not return null.")
        public void test1() {
            ResolverProvider<ServiceLoaderResolver> provider = 
                ServiceLoaderResolver.provider();

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should return an instance on get.")
        public void test2() {
            ResolverProvider<ServiceLoaderResolver> provider = 
                ServiceLoaderResolver.provider();

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should load properties from ServiceLoader resolvers.")
        void test1() {
            ServiceLoaderResolver resolver = resolverToTest();
            ProxyMethod javaVersionProxyMethod = ProxyMethods.javaVersion();
            ProxyMethod pathProxyMethod = ProxyMethods.path();


            Optional<String> javaVersion = resolver.resolve(
                javaVersionProxyMethod, 
                "java.version"
            );
            Optional<String> pathEnv = resolver.resolve(pathProxyMethod, "path");

            // From SystemPropertyResolver.
            assertNotNull(javaVersion);
            assertEquals(
                System.getProperty("java.version"), 
                javaVersion.get()
            );

            // From EnvironmentVariableResolver.
            assertNotNull(pathEnv);
            assertEquals(
                System.getenv("PATH"), 
                pathEnv.get()
            );
        }
        
        @Test
        @DisplayName(
            "should return empty Optional when property cannot be resolved from " + 
            "any of the ServiceLoader resolvers."
        )
        void test2() {
            ServiceLoaderResolver resolver = resolverToTest();
            ProxyMethod proxyMethod = ProxyMethods.property();

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    private ServiceLoaderResolver resolverToTest() {
        return new ServiceLoaderResolver();
    }
}
