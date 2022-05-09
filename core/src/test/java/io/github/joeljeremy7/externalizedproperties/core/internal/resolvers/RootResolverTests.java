package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.ResolverProvider;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootResolverTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    null,
                    Arrays.asList(DefaultResolver.provider()),
                    RootProcessor.provider(),
                    SimpleVariableExpander.provider()
                )
            );
        }

        @Test
        @DisplayName("should throw when resolver provider argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    ExternalizedProperties.builder().withDefaults().build(),
                    null,
                    RootProcessor.provider(),
                    SimpleVariableExpander.provider()
                )
            );
        }

        @Test
        @DisplayName("should throw when root processor provider argument is null")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    ExternalizedProperties.builder().withDefaults().build(),
                    Arrays.asList(DefaultResolver.provider()),
                    null,
                    SimpleVariableExpander.provider()
                )
            );
        }

        @Test
        @DisplayName("should throw when variable expander provider argument is null")
        void test4() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    ExternalizedProperties.builder().withDefaults().build(),
                    Arrays.asList(DefaultResolver.provider()),
                    RootProcessor.provider(),
                    null
                )
            );
        }
    }

    @Nested
    class ProviderMethod {
        @Test
        @DisplayName("should throw when resolver providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootResolver.provider(
                    null,
                    RootProcessor.provider(),
                    SimpleVariableExpander.provider()
                )
            );
        }

        @Test
        @DisplayName("should throw when root processor provider argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootResolver.provider(
                    Arrays.asList(DefaultResolver.provider()),
                    null,
                    SimpleVariableExpander.provider()
                )
            );
        }

        @Test
        @DisplayName("should throw when variable expander provider argument is null")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootResolver.provider(
                    Arrays.asList(DefaultResolver.provider()),
                    RootProcessor.provider(),
                    null
                )
            );
        }

        @Test
        @DisplayName("should not return null")
        void test4() {
            ResolverProvider<RootResolver> provider = 
                RootResolver.provider(
                    Arrays.asList(DefaultResolver.provider()),
                    RootProcessor.provider(),
                    SimpleVariableExpander.provider()
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test5() {
            ResolverProvider<RootResolver> provider = 
                RootResolver.provider(
                    Arrays.asList(DefaultResolver.provider()),
                    RootProcessor.provider(),
                    SimpleVariableExpander.provider()
                );

            assertNotNull(
                provider.get(ExternalizedProperties.builder().withDefaults().build())
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        public void test1() {
            RootResolver resolver = rootResolver(DefaultResolver.provider());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(null, "property")
            );
        }

        @Test
        @DisplayName("should throw when property name is null")
        public void test2() {
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            RootResolver resolver = rootResolver(
                DefaultResolver.provider()
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(proxyMethod, null)
            );
        }

        @Test
        @DisplayName("should resolve properties from registered resolvers")
        public void test3() {
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("property", "property-value");
            
            RootResolver resolver = rootResolver(
                MapResolver.provider(propertySource)
            );
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property
            );
            
            Optional<String> result = 
                resolver.resolve(proxyMethod, "property");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                propertySource.get("property"), 
                result.get()
            );
        }

        @Test
        @DisplayName("should expand variables in externalized property name")
        public void test4() {
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("property", "property-expanded");
            propertySource.put("property-expanded", "variable-expanded");
            
            RootResolver resolver = rootResolver(
                MapResolver.provider(propertySource)
            );
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyVariable
            );
            
            Optional<String> result = 
                resolver.resolve(proxyMethod, "${property}");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                propertySource.get("property-expanded"), 
                result.get()
            );
        }

        @Test
        @DisplayName("should process resolved properties via registered processors")
        public void test5() {
            String originalPropertyValue = "property-value";
            String base64EncodedPropertyValue = EncryptionUtils.encryptAesBase64(
                originalPropertyValue
            );
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("test.decrypt", base64EncodedPropertyValue);
            
            RootResolver resolver = rootResolver(
                Arrays.asList(MapResolver.provider(propertySource)),
                RootProcessor.provider(
                    DecryptProcessor.provider(getAesDecryptor())
                )
            );
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyDecrypt
            );
            
            Optional<String> result = 
                resolver.resolve(proxyMethod, "test.decrypt");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(originalPropertyValue, result.get());
        }
    }

    private RootResolver rootResolver(
            ResolverProvider<?>... resolverProviders
    ) {
        return rootResolver(
            Arrays.asList(resolverProviders), 
            RootProcessor.provider()
        );
    }

    private RootResolver rootResolver(
            Collection<ResolverProvider<?>> resolverProviders,
            ProcessorProvider<RootProcessor> rootProcessorProvider
    ) {
        return new RootResolver(
            ExternalizedProperties.builder()
                .resolvers(resolverProviders)
                .processors(rootProcessorProvider)
                .build(), 
            resolverProviders, 
            rootProcessorProvider, 
            SimpleVariableExpander.provider()
        );
    }

    private static Decryptor getAesDecryptor() {
        try {
            return JceDecryptor.factory().symmetric(
                EncryptionUtils.AES_GCM_ALGORITHM, 
                EncryptionUtils.DEFAULT_AES_SECRET_KEY,
                EncryptionUtils.DEFAULT_GCM_PARAMETER_SPEC 
            );
        } catch (InvalidKeyException | 
                NoSuchAlgorithmException | 
                NoSuchPaddingException | 
                InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Cannot instantiate decryptor.", e);
        }
    }

    public static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("${property}")
        String propertyVariable();

        @ExternalizedProperty("test.decrypt")
        @Decrypt(EncryptionUtils.AES_GCM_ALGORITHM)
        String propertyDecrypt();
    }
}
