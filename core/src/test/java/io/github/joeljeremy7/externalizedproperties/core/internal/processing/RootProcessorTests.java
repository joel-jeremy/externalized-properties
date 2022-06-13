package io.github.joeljeremy7.externalizedproperties.core.internal.processing;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Processor;
import io.github.joeljeremy7.externalizedproperties.core.processing.Decrypt;
import io.github.joeljeremy7.externalizedproperties.core.processing.ProcessingException;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils.AES_GCM_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RootProcessorTests {
    private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
        InvocationContextUtils.testFactory(ProxyInterface.class);
    
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when processors varargs argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    (Processor[])null
                )
            );
        }

        @Test
        @DisplayName("should throw when processors collection argument is null")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new RootProcessor(
                    (Collection<Processor>)null
                )
            );
        }
    }

    @Nested
    class ProcessMethod {
        // @Test
        // @DisplayName("should throw when proxy method argument is null")
        // void test1() {
        //     RootProcessor processor = rootProcessor();

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> processor.process(null, "valueToProcess")
        //     );
        // }

        // @Test
        // @DisplayName("should throw when context argument is null")
        // void test2() {
        //     RootProcessor processor = rootProcessor(
        //         new DecryptProcessor(createAesDecryptor())
        //     );

        //     InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
        //         ProxyInterface::decrypt,
        //         externalizedProperties(processor)
        //     );

        //     assertThrows(
        //         IllegalArgumentException.class, 
        //         () -> processor.process(context, null)
        //     );
        // }

        @Test
        @DisplayName("should process property using configured processor")
        void test1() {
            RootProcessor processor = rootProcessor(
                new DecryptProcessor(createAesDecryptor())
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::decrypt,
                externalizedProperties(processor)
            );

            String plainText = "plain-text-value";
            String encryptedBase64Encoded = EncryptionUtils.encryptAesBase64(plainText);

            String result = processor.process(
                context, 
                encryptedBase64Encoded
            );

            assertEquals(plainText, result);
        }

        @Test
        @DisplayName("should throw when required processor is not registered")
        void test2() {
            Processor stubProcessor = new Processor() {
                @Override
                public String process(InvocationContext context, String valueToProcess) {
                    return valueToProcess;
                }
            };

            RootProcessor processor = rootProcessor(
                stubProcessor
                // Base64Decode processor not registered.
            );

            InvocationContext context = INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::decrypt,
                externalizedProperties(processor)
            );

            String plainText = "plain-text-value";
            String encryptedBase64Encoded = EncryptionUtils.encryptAesBase64(plainText);

            assertThrows(
                ProcessingException.class, 
                () -> processor.process(context, encryptedBase64Encoded))
            ;
        }
    }

    private static RootProcessor rootProcessor(Processor... processors) {
        return new RootProcessor(processors);
    }
    
    private static ExternalizedProperties externalizedProperties(
            Processor... processors
    ) {
        return ExternalizedProperties.builder()
            .processors(processors)
            .build();
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

    private static interface ProxyInterface {
        @ExternalizedProperty("test.decrypt")
        @Decrypt(AES_GCM_ALGORITHM)
        String decrypt();
    }
}
