package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy7.externalizedproperties.core.conversion.converters.PrimitiveConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
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

public class ConverterProviderTests {
    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when converter argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ConverterProvider.of(null)
            );
        }

        @Test
        @DisplayName("should always return the provided converter instance")
        void test2() {
            PrimitiveConverter converter = new PrimitiveConverter();
            ConverterProvider<?> provider = ConverterProvider.of(converter);
            
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            RootConverter rootConverter = new RootConverter(
                externalizedProperties, 
                provider
            );   

            assertSame(converter, provider.get(externalizedProperties, rootConverter));
        }
    }

    @Nested
    class MemoizeMethod {
        @Test
        @DisplayName("should throw when the provider to memoize argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ConverterProvider.memoize(null)
            );
        }

        @Test
        @DisplayName("should throw when memoized provider returns null")
        public void test2() {
            ConverterProvider<?> provider = (e, rc) -> null;
            ConverterProvider<?> memoized = ConverterProvider.memoize(provider);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(memoized)
                    .build();
            
            assertThrows(
                IllegalStateException.class, 
                () -> memoized.get(
                    externalizedProperties,
                    new RootConverter(externalizedProperties, memoized)
                )
            );
        }

        @Test
        @DisplayName("should memoize result of the provider to memoize argument")
        public void test3() {
            ConverterProvider<DefaultConverter> provider = DefaultConverter.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            RootConverter rootConverter = new RootConverter(
                externalizedProperties, 
                provider
            );
            
            ConverterProvider<DefaultConverter> memoized = 
                ConverterProvider.memoize(provider);

            DefaultConverter instance1 = memoized.get(
                externalizedProperties,
                rootConverter
            );
            DefaultConverter instance2 = memoized.get(
                externalizedProperties,
                rootConverter
            );

            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should memoize result of the provider to memoize argument " +
            "in multithreaded environment"
        )
        public void test4() throws InterruptedException {
            ConverterProvider<?> provider = DefaultConverter.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            RootConverter rootConverter = new RootConverter(
                externalizedProperties, 
                provider
            );

            int numberOfCompetingThreads = 16;
            ExecutorService executor = Executors.newFixedThreadPool(
                numberOfCompetingThreads
            );
            CountDownLatch latch = new CountDownLatch(numberOfCompetingThreads);
            List<Converter<?>> instances = new CopyOnWriteArrayList<>();

            ConverterProvider<?> memoized = ConverterProvider.memoize(provider);
            for (int i = 0; i < numberOfCompetingThreads; i++) {
                executor.submit(() -> {
                    // Should return the same instance everytime.
                    instances.add(
                        memoized.get(externalizedProperties, rootConverter)
                    );
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            assertEquals(numberOfCompetingThreads, instances.size());

            Converter<?> first = instances.get(0);
            for (Converter<?> resolver : instances) {
                // All the same instances.
                assertSame(first, resolver);
            }
        }

        @Test
        @DisplayName("should return different instance for each memoized provider")
        public void test5() {
            ConverterProvider<DefaultConverter> provider = DefaultConverter.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .converters(provider)
                    .build();
            RootConverter rootConverter = new RootConverter(
                externalizedProperties, 
                provider
            );
            
            ConverterProvider<DefaultConverter> memoized1 = 
                ConverterProvider.memoize(provider);
            ConverterProvider<DefaultConverter> memoized2 = 
                ConverterProvider.memoize(provider);

            DefaultConverter instance1 = memoized1.get(
                externalizedProperties,
                rootConverter
            );
            DefaultConverter instance2 = memoized2.get(
                externalizedProperties,
                rootConverter
            );

            assertNotSame(instance1, instance2);
        }
    } 
}
