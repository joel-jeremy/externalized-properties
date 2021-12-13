package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionContext;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
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
        private ProxyInterface proxyInterface;
        private ProxyInterface proxyInterfaceWithCaching;

        @Setup
        public void setup() {
            /**
             * Basic setup. No caching.
             */
            externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(
                    Collections.singletonMap("testInt", "1")
                ))
                .conversionHandlers(new IntegerConversionHandler())
                .build();

            proxyInterface = externalizedProperties.proxy(ProxyInterface.class);

            /**
             * Setup with caching.
             */
            externalizedPropertiesWithCaching = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(
                    Collections.singletonMap("testInt", "1")
                ))
                .withCaching()
                .withCacheDuration(Duration.ofHours(24))
                .build();

            proxyInterfaceWithCaching = 
                externalizedPropertiesWithCaching.proxy(ProxyInterface.class);
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
    public Optional<Integer> conversionInResolveProperty(BenchmarkState state) {
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
    public Optional<Integer> conversionInResolvePropertyWithCaching(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.resolveProperty("testInt", Integer.class);
    }

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int conversionInProxyInterfaceWithCaching(BenchmarkState state) {
        return state.proxyInterfaceWithCaching.testInt();
    }

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int conversionInProxyInterface(BenchmarkState state) {
        return state.proxyInterface.testInt();
    }

    private static class IntegerConversionHandler implements ConversionHandler<Integer> {
        @Override
        public boolean canConvertTo(Class<?> targetType) {
            return Integer.TYPE.equals(targetType) || Integer.class.equals(targetType);
        }

        @Override
        public Integer convert(ConversionContext context) {
            return Integer.valueOf(context.value());
        }
    }
}
