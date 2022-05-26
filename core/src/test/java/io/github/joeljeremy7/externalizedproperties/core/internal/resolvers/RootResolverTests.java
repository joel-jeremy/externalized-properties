package io.github.joeljeremy7.externalizedproperties.core.internal.resolvers;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.TestProxyMethodFactory;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootResolverTests {
    private static final TestProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new TestProxyMethodFactory<>(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolver provider argument is null")
        void test1() {
            RootProcessor rootProcessor = new RootProcessor();
            VariableExpander variableExpander = new SimpleVariableExpander();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    null,
                    rootProcessor,
                    variableExpander
                )
            );
        }

        @Test
        @DisplayName("should throw when root processor provider argument is null")
        void test2() {
            List<Resolver> resolvers = Arrays.asList(new DefaultResolver());
            VariableExpander variableExpander = new SimpleVariableExpander();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    resolvers,
                    null,
                    variableExpander
                )
            );
        }

        @Test
        @DisplayName("should throw when variable expander provider argument is null")
        void test3() {
            List<Resolver> resolvers = Arrays.asList(new DefaultResolver());
            RootProcessor rootProcessor = new RootProcessor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootResolver(
                    resolvers,
                    rootProcessor,
                    null
                )
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        void test1() {
            RootResolver resolver = rootResolver(Arrays.asList(new DefaultResolver()));
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(null, "property")
            );
        }

        @Test
        @DisplayName("should throw when property name is null")
        void test2() {
            List<Resolver> resolvers = Arrays.asList(
                new DefaultResolver()
            );

            RootResolver resolver = rootResolver(resolvers);

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolvers)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(proxyMethod, null)
            );
        }

        @Test
        @DisplayName("should resolve properties from registered resolvers")
        void test3() {
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("property", "property-value");
            
            List<Resolver> resolvers = Arrays.asList(
                new MapResolver(propertySource)
            );

            RootResolver resolver = rootResolver(resolvers);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::property,
                externalizedProperties(resolvers)
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
        void test4() {
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("property", "property-expanded");
            propertySource.put("property-expanded", "variable-expanded");

            List<Resolver> resolvers = Arrays.asList(
                new MapResolver(propertySource)
            );

            RootResolver resolver = rootResolver(resolvers);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyVariable,
                externalizedProperties(resolvers)
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
        void test5() {
            String originalPropertyValue = "property-value";
            String base64EncodedPropertyValue = EncryptionUtils.encryptAesBase64(
                originalPropertyValue
            );
            Map<String, String> propertySource = new HashMap<>();
            propertySource.put("test.decrypt", base64EncodedPropertyValue);

            List<Resolver> resolvers = Arrays.asList(
                new MapResolver(propertySource)
            );
            Processor processor = new DecryptProcessor(getAesDecryptor());
            
            RootResolver resolver = rootResolver(resolvers, processor);
            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::propertyDecrypt,
                externalizedProperties(resolvers, processor)
            );
            
            Optional<String> result = 
                resolver.resolve(proxyMethod, "test.decrypt");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(originalPropertyValue, result.get());
        }
    }

    private static RootResolver rootResolver(
            Collection<Resolver> resolvers,
            Processor... processors
    ) {
        return rootResolver(
            resolvers,
            new RootProcessor(Arrays.asList(processors))
        );
    }

    private static RootResolver rootResolver(
            Collection<Resolver> resolvers,
            RootProcessor rootProcessor
    ) {
        return new RootResolver(
            resolvers, 
            rootProcessor, 
            new SimpleVariableExpander()
        );
    }
    
    private static ExternalizedProperties externalizedProperties(
            Collection<Resolver> resolvers,
            Processor... processors
    ) {
        return ExternalizedProperties.builder()
            .resolvers(resolvers)
            .processors(processors)
            .build();
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

    private static interface ProxyInterface {
        @ExternalizedProperty("property")
        String property();

        @ExternalizedProperty("${property}")
        String propertyVariable();

        @ExternalizedProperty("test.decrypt")
        @Decrypt(EncryptionUtils.AES_GCM_ALGORITHM)
        String propertyDecrypt();
    }
}
