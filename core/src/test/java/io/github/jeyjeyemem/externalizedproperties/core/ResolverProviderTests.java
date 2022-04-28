package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.resolvers.DefaultResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testfixtures.StubResolver;
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

public class ResolverProviderTests {
    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when resolver argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolverProvider.of(null)
            );
        }

        @Test
        @DisplayName("should always return the provided resolver instance")
        void test2() {
            StubResolver resolver = new StubResolver();
            ResolverProvider<?> provider = ResolverProvider.of(resolver);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .resolvers(provider)
                    .build();

            assertSame(resolver, provider.get(externalizedProperties));
        }
    }
    
    @Nested
    class MemoizeMethod {
        @Test
        @DisplayName("should throw when the provider to memoize argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolverProvider.memoize(null)
            );
        }

        @Test
        @DisplayName("should throw when memoized provider returns null")
        void test2() {
            ResolverProvider<?> provider = e -> null;
            ResolverProvider<?> memoized = ResolverProvider.memoize(provider);
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .resolvers(memoized)
                    .build();
            
            assertThrows(
                IllegalStateException.class, 
                () -> memoized.get(externalizedProperties)
            );
        }

        @Test
        @DisplayName("should memoize result of the provider to memoize argument")
        void test3() {
            ResolverProvider<DefaultResolver> provider = DefaultResolver.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .resolvers(provider)
                    .build();
            
            ResolverProvider<DefaultResolver> memoized = 
                ResolverProvider.memoize(provider);

            DefaultResolver instance1 = memoized.get(externalizedProperties);
            DefaultResolver instance2 = memoized.get(externalizedProperties);

            assertSame(instance1, instance2);
        }

        @Test
        @DisplayName(
            "should memoize result of the provider to memoize argument " +
            "in multithreaded environment"
        )
        void test4() throws InterruptedException {
            ResolverProvider<?> provider = DefaultResolver.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .resolvers(provider)
                    .build();

            int numberOfCompetingThreads = 8;
            ExecutorService executor = Executors.newFixedThreadPool(
                numberOfCompetingThreads
            );
            CountDownLatch latch = new CountDownLatch(numberOfCompetingThreads);
            List<Resolver> instances = new CopyOnWriteArrayList<>();

            ResolverProvider<?> memoized = ResolverProvider.memoize(provider);
            for (int i = 0; i < numberOfCompetingThreads; i++) {
                executor.submit(() -> {
                    // Should return the same instance everytime.
                    instances.add(memoized.get(externalizedProperties));
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);

            assertEquals(numberOfCompetingThreads, instances.size());

            Resolver first = instances.get(0);
            for (Resolver resolver : instances) {
                // All the same instances.
                assertSame(first, resolver);
            }
        }

        @Test
        @DisplayName("should return different instance for each memoized provider")
        void test5() throws InterruptedException {
            ResolverProvider<DefaultResolver> provider = DefaultResolver.provider();
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .withDefaultResolvers()
                    .resolvers(provider)
                    .build();
            
            ResolverProvider<DefaultResolver> memoized1 = 
                ResolverProvider.memoize(provider);
            ResolverProvider<DefaultResolver> memoized2 = 
                ResolverProvider.memoize(provider);

            DefaultResolver instance1 = memoized1.get(externalizedProperties);
            DefaultResolver instance2 = memoized2.get(externalizedProperties);

            assertNotSame(instance1, instance2);
        }
    }
}
