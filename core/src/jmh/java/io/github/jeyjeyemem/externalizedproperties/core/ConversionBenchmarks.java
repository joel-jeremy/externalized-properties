package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.proxy.ProxyMethod;
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

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark property conversion via proxy or directly from 
 * {@link ExternalizedProperties}.
 */
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public abstract class ConversionBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private String key;
        private Map<String, String> baselineMap;
        private ConversionProxyInterface proxyInterface;
        private ConversionProxyInterface proxyInterfaceWithEagerLoading;
        private ConversionProxyInterface proxyInterfaceWithInvocationCaching;

        @Setup
        public void setup() throws NoSuchMethodException, SecurityException {
            key = "testInt";

            baselineMap = new ConcurrentHashMap<>();
            baselineMap.put(key, "1");

            System.setProperty(key, "1");

            /**
             * Basic setup. No caching.
             */
            ExternalizedProperties externalizedProperties =     
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(
                        Collections.singletonMap(key, "1")
                    ))
                    .converters((ep, rc) -> new IntegerConverter())
                    .build();

            proxyInterface = externalizedProperties.proxy(ConversionProxyInterface.class);

            /**
             * Setup with eager loading.
             */
            ExternalizedProperties externalizedPropertiesWithEagerLoading = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(
                        Collections.singletonMap(key, "1")
                    ))
                    .withProxyEagerLoading()
                    .withCacheDuration(Duration.ofHours(24))
                    .converters((ep, rc) -> new IntegerConverter())
                    .build();

            proxyInterfaceWithEagerLoading = 
                externalizedPropertiesWithEagerLoading.proxy(
                    ConversionProxyInterface.class
                );
            
            /**
             * Setup with eager loading.
             */
            ExternalizedProperties externalizedPropertiesWithInvocationCaching = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(
                        Collections.singletonMap(key, "1")
                    ))
                    .withProxyInvocationCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .converters((ep, rc) -> new IntegerConverter())
                    .build();

            proxyInterfaceWithInvocationCaching = 
                externalizedPropertiesWithInvocationCaching.proxy(
                    ConversionProxyInterface.class
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
     * Benchmark retrieval of items from a map and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int baselineConcurrentMap(BenchmarkState state) {
        return Integer.valueOf(state.baselineMap.get(state.key));
    }

    /**
     * Benchmark retrieval of items from system properties and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int baselineSystemProperty(BenchmarkState state) {
        return Integer.valueOf(System.getProperty(state.key));
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
     * Benchmark resolution of properties from a proxy interface with proxy
     * eager loading enabled and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithEagerLoading(BenchmarkState state) {
        return state.proxyInterfaceWithEagerLoading.testInt();
    }

    /**
     * Benchmark resolution of properties from a proxy interface with proxy
     * eager loading enabled and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithInvocationCaching(BenchmarkState state) {
        return state.proxyInterfaceWithInvocationCaching.testInt();
    }

    private static class IntegerConverter implements Converter<Integer> {
        @Override
        public boolean canConvertTo(Class<?> targetType) {
            return Integer.TYPE.equals(targetType);
        }

        @Override
        public ConversionResult<Integer> convert(
                ProxyMethod proxyMethod, 
                String valueToConvert, 
                Type targetType
        ) {
            return ConversionResult.of(Integer.valueOf(valueToConvert));
        }
    }
}
