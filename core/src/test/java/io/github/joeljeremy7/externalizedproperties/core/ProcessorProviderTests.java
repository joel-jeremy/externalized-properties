package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.Decryptor;
import io.github.joeljeremy7.externalizedproperties.core.processing.processors.DecryptProcessor.JceDecryptor;
import io.github.joeljeremy7.externalizedproperties.core.testentities.EncryptionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProcessorProviderTests {
    private static final String AES_ALGORITHM = "AES";
    private static final SecretKey AES_SECRET_KEY = EncryptionUtils.generateAesSecretKey();

    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when processor argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ProcessorProvider.of(null)
            );
        }

        @Test
        @DisplayName("should always return the provided processor instance")
        void test2() {
            DecryptProcessor processor = new DecryptProcessor(createAesDecryptor());
            ProcessorProvider<?> provider = ProcessorProvider.of(processor);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();

            assertSame(processor, provider.get(externalizedProperties));
        }
    }

    @Nested
    class MemoizeMethod {
        @Test
        @DisplayName("should throw when the provider to memoize argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ProcessorProvider.memoize(null)
            );
        }

        @Test
        @DisplayName("should throw when memoized provider returns null")
        void test2() {
            ProcessorProvider<?> provider = e -> null;
            ProcessorProvider<?> memoized = ProcessorProvider.memoize(provider);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(memoized)
                    .build();
            
            assertThrows(
                IllegalStateException.class, 
                () -> memoized.get(externalizedProperties)
            );
        }

        @Test
        @DisplayName("should memoize result of the provider to memoize argument")
        void test3() {
            ProcessorProvider<DecryptProcessor> provider = 
                DecryptProcessor.provider(createAesDecryptor());
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();
            
            ProcessorProvider<DecryptProcessor> memoized = 
                ProcessorProvider.memoize(provider);

            DecryptProcessor instance1 = memoized.get(externalizedProperties);
            DecryptProcessor instance2 = memoized.get(externalizedProperties);

            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should memoize result of the provider to memoize argument " + 
            "in multithreaded environment"
        )
        void test4() throws InterruptedException {
            ProcessorProvider<?> provider = DecryptProcessor.provider(createAesDecryptor());
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();

            int numberOfCompetingThreads = 16;
            ExecutorService executor = Executors.newFixedThreadPool(
                numberOfCompetingThreads
            );
            CountDownLatch latch = new CountDownLatch(numberOfCompetingThreads);
            List<Processor> instances = new CopyOnWriteArrayList<>();

            ProcessorProvider<?> memoized = ProcessorProvider.memoize(provider);
            for (int i = 0; i < numberOfCompetingThreads; i++) {
                executor.submit(() -> {
                    // Should return the same instance everytime.
                    instances.add(memoized.get(externalizedProperties));
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            assertEquals(numberOfCompetingThreads, instances.size());

            Processor first = instances.get(0);
            for (Processor resolver : instances) {
                // All the same instances.
                assertSame(first, resolver);
            }
        }

        @Test
        @DisplayName("should return different instance for each memoized provider")
        void test5() {
            ProcessorProvider<DecryptProcessor> provider = 
                DecryptProcessor.provider(createAesDecryptor());
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();
            
            ProcessorProvider<DecryptProcessor> memoized1 = 
                ProcessorProvider.memoize(provider);
            ProcessorProvider<DecryptProcessor> memoized2 = 
                ProcessorProvider.memoize(provider);

            DecryptProcessor instance1 = memoized1.get(externalizedProperties);
            DecryptProcessor instance2 = memoized2.get(externalizedProperties);

            assertNotSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should return same provider instance when provider was already memoized"
        )
        void test6() {
            ProcessorProvider<DecryptProcessor> provider = 
                DecryptProcessor.provider(createAesDecryptor());
            
            ProcessorProvider<DecryptProcessor> memoized1 = 
                ProcessorProvider.memoize(provider);

            ProcessorProvider<DecryptProcessor> sameAsMemoized1 = 
                ProcessorProvider.memoize(memoized1);

            assertSame(memoized1, sameAsMemoized1);
        }
    } 

    private static Decryptor createAesDecryptor() {
        try {
            return JceDecryptor.factory().symmetric(AES_ALGORITHM, AES_SECRET_KEY);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Cannot instantiate decryptor.", e);
        }
    }
}
