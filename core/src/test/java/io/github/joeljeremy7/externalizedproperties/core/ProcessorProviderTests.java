package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.processing.Base64DecodeProcessor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            Base64DecodeProcessor processor = new Base64DecodeProcessor();
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
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ProcessorProvider.memoize(null)
            );
        }

        @Test
        @DisplayName("should throw when memoized provider returns null")
        public void test2() {
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
        public void test3() {
            ProcessorProvider<Base64DecodeProcessor> provider = 
                Base64DecodeProcessor.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();
            
            ProcessorProvider<Base64DecodeProcessor> memoized = 
                ProcessorProvider.memoize(provider);

            Base64DecodeProcessor instance1 = memoized.get(externalizedProperties);
            Base64DecodeProcessor instance2 = memoized.get(externalizedProperties);

            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should memoize result of the provider to memoize argument " + 
            "in multithreaded environment"
        )
        public void test4() throws InterruptedException {
            ProcessorProvider<?> provider = Base64DecodeProcessor.provider();
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
        public void test5() {
            ProcessorProvider<Base64DecodeProcessor> provider = 
                Base64DecodeProcessor.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .processors(provider)
                    .build();
            
            ProcessorProvider<Base64DecodeProcessor> memoized1 = 
                ProcessorProvider.memoize(provider);
            ProcessorProvider<Base64DecodeProcessor> memoized2 = 
                ProcessorProvider.memoize(provider);

            Base64DecodeProcessor instance1 = memoized1.get(externalizedProperties);
            Base64DecodeProcessor instance2 = memoized2.get(externalizedProperties);

            assertNotSame(instance1, instance2);
        }
    } 
}
