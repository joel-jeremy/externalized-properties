package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.EnumConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapResolver;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Benchmark property conversion via proxy or directly from 
 * {@link ExternalizedProperties}.
 */
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public abstract class ConversionBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private ExternalizedProperties externalizedProperties;
        private ExternalizedProperties externalizedPropertiesWithCaching;
        private ExternalizedProperties externalizedPropertiesWithMultipleConversionHandlers;
        private ProxyInterface proxyInterface;
        private ProxyInterface proxyInterfaceWithCaching;
        private ProxyInterface proxyInterfaceWithMultipleConversionHandler;

        @Setup
        public void setup() {
            /**
             * Basic setup. No caching.
             */
            externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapResolver(
                    Collections.singletonMap("testInt", "1")
                ))
                .withDefaultConversionHandlers()
                .build();

            proxyInterface = externalizedProperties.proxy(ProxyInterface.class);

            /**
             * Setup with caching.
             */
            externalizedPropertiesWithCaching = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapResolver(
                    Collections.singletonMap("testInt", "1")
                ))
                .withCaching()
                .withCacheDuration(Duration.ofHours(24))
                .withDefaultConversionHandlers()
                .build();

            proxyInterfaceWithCaching = 
                externalizedPropertiesWithCaching.proxy(ProxyInterface.class);

            /**
             * Basic setup. No caching.
             */
            externalizedPropertiesWithMultipleConversionHandlers = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(new MapResolver(
                        Collections.singletonMap("testInt", "1")
                    ))
                    // Add a bunch more conversion handlers.
                    .conversionHandlers(
                        Stream.generate(() -> new EnumConversionHandler())
                            .limit(50)
                            .toArray(ConversionHandler<?>[]::new)
                    )
                    .withDefaultConversionHandlers()
                    .build();

            proxyInterfaceWithMultipleConversionHandler = 
                externalizedPropertiesWithMultipleConversionHandlers.proxy(
                    ProxyInterface.class
                );
        }
    }

    /**
     * Benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class ConversionBenchmarksAvgt extends ConversionBenchmarks {}

    /**
     * Benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class ConversionBenchmarksThrpt extends ConversionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(Threads.MAX)
    public static class ConversionBenchmarksAvgtMultiThreaded extends ConversionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(Threads.MAX)
    public static class ConversionBenchmarksThrptMultiThreaded extends ConversionBenchmarks {}

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<Integer> resolveProperty(BenchmarkState state) {
        return state.externalizedProperties.resolveProperty("testInt", Integer.class);
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<Integer> resolvePropertyWithCaching(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.resolveProperty("testInt", Integer.class);
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<Integer> resolvePropertyWithMultipleConversionHandlers(
            BenchmarkState state
    ) {
        return state.externalizedPropertiesWithMultipleConversionHandlers.resolveProperty(
            "testInt", 
            Integer.class
        );
    }

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterface(BenchmarkState state) {
        return state.proxyInterface.testInt();
    }

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithCaching(BenchmarkState state) {
        return state.proxyInterfaceWithCaching.testInt();
    }

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithMultipleConversionHandler(BenchmarkState state) {
        return state.proxyInterfaceWithMultipleConversionHandler.testInt();
    }

    // private static class IntegerConversionHandler implements ConversionHandler<Integer> {
    //     @Override
    //     public boolean canConvertTo(Class<?> targetType) {
    //         return Integer.TYPE.equals(targetType) || Integer.class.equals(targetType);
    //     }

    //     @Override
    //     public Integer convert(ConversionContext context) {
    //         return Integer.valueOf(context.value());
    //     }
    // }
}
