package io.github.joeljeremy7.externalizedproperties.core.internal.processing;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.ProcessorProvider;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.ProxyMethodFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RootProcessorTests {
    private static final ProxyMethodFactory<ProxyInterface> PROXY_METHOD_FACTORY =
        new ProxyMethodFactory<>(ProxyInterface.class);
    private static final ExternalizedProperties EXTERNALIZED_PROPERTIES = 
        ExternalizedProperties.builder().withDefaults().build();
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when externalized properties argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    null,
                    ep -> new DecryptProcessor(createAesDecryptor())
                )
            );
        }

        @Test
        @DisplayName("should throw when processors varargs argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    EXTERNALIZED_PROPERTIES,
                    (ProcessorProvider[])null
                )
            );
        }

        @Test
        @DisplayName("should throw when processors collection argument is null")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    EXTERNALIZED_PROPERTIES,
                    (Collection<ProcessorProvider<?>>)null
                )
            );
        }
    }

    @Nested
    class ProviderMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when processor providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootProcessor.provider((ProcessorProvider<?>[])null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(DecryptProcessor.provider(createAesDecryptor()));

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(DecryptProcessor.provider(createAesDecryptor()));

            assertNotNull(provider.get(EXTERNALIZED_PROPERTIES));
        }
    }

    @Nested
    class ProviderMethodCollectionOverload {
        @Test
        @DisplayName("should throw when processor providers argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> RootProcessor.provider((Collection<ProcessorProvider<?>>)null)
            );
        }

        @Test
        @DisplayName("should not return null")
        void test2() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(
                    Arrays.asList(DecryptProcessor.provider(createAesDecryptor()))
                );

            assertNotNull(provider);
        }

        @Test
        @DisplayName("should not return null on get")
        void test3() {
            ProcessorProvider<RootProcessor> provider = 
                RootProcessor.provider(
                    Arrays.asList(DecryptProcessor.provider(createAesDecryptor()))
                );

            assertNotNull(provider.get(EXTERNALIZED_PROPERTIES));
        }
    }

    @Nested
    class ProcessMethod {
        @Test
        @DisplayName("should throw when proxy method argument is null")
        void test1() {
            RootProcessor processor = rootProcessor();

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(null, "valueToProcess")
            );
        }
        @Test
        @DisplayName("should throw when context argument is null")
        void test2() {
            RootProcessor processor = rootProcessor(
                DecryptProcessor.provider(createAesDecryptor())
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decrypt
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> processor.process(proxyMethod, null)
            );
        }

        @Test
        @DisplayName(
            "should process property using configured processor classes"
        )
        void test3() {
            RootProcessor processor = rootProcessor(
                DecryptProcessor.provider(createAesDecryptor())
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decrypt
            );

            String plainText = "plain-text-value";
            String encryptedBase64Encoded = EncryptionUtils.encryptAesBase64(plainText);

            String result = processor.process(
                proxyMethod, 
                encryptedBase64Encoded
            );

            assertEquals(plainText, result);
        }

        @Test
        @DisplayName(
            "should when required processor class is not configured"
        )
        void test4() {
            Processor stubProcessor = new Processor() {
                @Override
                public String process(ProxyMethod proxyMethod, String valueToProcess) {
                    return valueToProcess;
                }
            };

            RootProcessor processor = rootProcessor(
                ep -> stubProcessor
                // Base64Decode processor not configured.
            );

            ProxyMethod proxyMethod = PROXY_METHOD_FACTORY.fromMethodReference(
                ProxyInterface::decrypt
            );

            String plainText = "plain-text-value";
            String encryptedBase64Encoded = EncryptionUtils.encryptAesBase64(plainText);

            assertThrows(
                ProcessingException.class, 
                () -> processor.process(proxyMethod, encryptedBase64Encoded))
            ;
        }
    }

    private RootProcessor rootProcessor(
            ProcessorProvider<?>... processorProviders
    ) {
        return new RootProcessor(
            ExternalizedProperties.builder()
                .withDefaultResolvers()
                .processors(processorProviders)
                .build(),
            processorProviders
        );
    }

    private static Decryptor createAesDecryptor() {
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
        @ExternalizedProperty("test.decrypt")
        @Decrypt(EncryptionUtils.AES_GCM_ALGORITHM)
        String decrypt();
    }
}
