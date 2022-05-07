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

/**
 * Service loader resolvers are configured in resources/META-INF/services folder.
 */
public class ServiceLoaderResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
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
            ProxyMethod javaVersionProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::javaVersion
            );
            ProxyMethod pathProxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::path
            );

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
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::notFound
            );

            Optional<String> result = resolver.resolve(
                proxyMethod, 
                "non.found"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    private ServiceLoaderResolver resolverToTest() {
        return new ServiceLoaderResolver();
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("java.version")
        String javaVersion();

        @ExternalizedProperty("path")
        String path();

        @ExternalizedProperty("property")
        String notFound();
    }
}
