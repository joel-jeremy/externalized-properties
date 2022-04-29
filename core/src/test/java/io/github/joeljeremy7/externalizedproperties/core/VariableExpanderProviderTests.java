package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;
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

public class VariableExpanderProviderTests {
    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when processor argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> VariableExpanderProvider.of(null)
            );
        }

        @Test
        @DisplayName("should always return the provided processor instance")
        void test2() {
            DummyVariableExpander variableExpander = new DummyVariableExpander();
            VariableExpanderProvider<?> provider = 
                VariableExpanderProvider.of(variableExpander);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .variableExpander(provider)
                    .build();

            assertSame(variableExpander, provider.get(externalizedProperties));
        }
    }

    @Nested
    class MemoizeMethod {
        @Test
        @DisplayName("should throw when the provider to memoize argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> VariableExpanderProvider.memoize(null)
            );
        }

        @Test
        @DisplayName("should throw when memoized provider returns null")
        void test2() {
            VariableExpanderProvider<?> provider = e -> null;
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .variableExpander(provider)
                    .build();
            
            VariableExpanderProvider<?> memoized = VariableExpanderProvider.memoize(provider);
            assertThrows(
                IllegalStateException.class, 
                () -> memoized.get(externalizedProperties)
            );
        }

        @Test
        @DisplayName("should memoize result of the provider to memoize argument")
        void test3() {
            VariableExpanderProvider<DummyVariableExpander> provider = 
                DummyVariableExpander.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .variableExpander(provider)
                    .build();
            
            VariableExpanderProvider<DummyVariableExpander> memoized = 
                VariableExpanderProvider.memoize(provider);

            DummyVariableExpander instance1 = memoized.get(externalizedProperties);
            DummyVariableExpander instance2 = memoized.get(externalizedProperties);

            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should memoize result of the provider to memoize argument " +
            "in multithreaded environment"
        )
        void test4() throws InterruptedException {
            // Use dummy variable expander to avoid calling proxy(...)
            // in constructor of SimpleVariableExpander.
            VariableExpanderProvider<?> provider = DummyVariableExpander.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .variableExpander(provider)
                    .build();

            int numberOfCompetingThreads = 8;
            ExecutorService executor = Executors.newFixedThreadPool(
                numberOfCompetingThreads
            );
            CountDownLatch latch = new CountDownLatch(numberOfCompetingThreads);
            List<VariableExpander> instances = new CopyOnWriteArrayList<>();

            VariableExpanderProvider<?> memoized = VariableExpanderProvider.memoize(provider);
            for (int i = 0; i < numberOfCompetingThreads; i++) {
                executor.submit(() -> {
                    // Should return the same instance everytime.
                    instances.add(memoized.get(externalizedProperties));
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            assertEquals(numberOfCompetingThreads, instances.size());

            VariableExpander first = instances.get(0);
            for (VariableExpander resolver : instances) {
                // All the same instances.
                assertSame(first, resolver);
            }
        }

        @Test
        @DisplayName("should return different instance for each memoized provider")
        void test5() {
            VariableExpanderProvider<DummyVariableExpander> provider = 
                DummyVariableExpander.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .variableExpander(provider)
                    .build();
            
            VariableExpanderProvider<DummyVariableExpander> memoized1 = 
                VariableExpanderProvider.memoize(provider);
            VariableExpanderProvider<DummyVariableExpander> memoized2 = 
                VariableExpanderProvider.memoize(provider);

            DummyVariableExpander instance1 = memoized1.get(externalizedProperties);
            DummyVariableExpander instance2 = memoized2.get(externalizedProperties);

            assertNotSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should return same provider instance when provider was already memoized"
        )
        void test6() {
            VariableExpanderProvider<DummyVariableExpander> provider = 
                DummyVariableExpander.provider();
            
            VariableExpanderProvider<DummyVariableExpander> memoized1 = 
                VariableExpanderProvider.memoize(provider);

            VariableExpanderProvider<DummyVariableExpander> sameAsMemoized1 = 
                VariableExpanderProvider.memoize(memoized1);

            assertSame(memoized1, sameAsMemoized1);
        }
    } 

    private static class DummyVariableExpander implements VariableExpander {
        @Override
        public String expandVariables(ProxyMethod proxyMethod, String value) {
            return value;
        }

        public static VariableExpanderProvider<DummyVariableExpander> provider() {
            return ep -> new DummyVariableExpander();
        }
    }
}
