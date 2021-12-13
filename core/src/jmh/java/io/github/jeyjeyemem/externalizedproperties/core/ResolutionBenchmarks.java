package io.github.jeyjeyemem.externalizedproperties.core;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark property resolution via proxy or directly from
 * {@link ExternalizedProperties}.
 */
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public abstract class ResolutionBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private Map<String, String> baselineMap;
        private ExternalizedProperties externalizedProperties;
        private ProxyInterface proxyInterface;
        private ProxyInterface proxyInterfaceWithCaching;
        private ProxyInterface proxyInterfaceWithInvocationCaching;
        private ProxyInterface proxyInterfaceWithEagerLoading;
        private ExternalizedProperties externalizedPropertiesWithCaching;

        @Setup
        public void setup() {
            baselineMap = new ConcurrentHashMap<>();
            baselineMap.put("test", "test");

            Map<String, String> propertySource = new ConcurrentHashMap<>();
            propertySource.put("test", "test");

            System.setProperty("test", "test");
    
            /**
             * Basic setup. No caching.
             */
            externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(propertySource))
                .build();

            proxyInterface = externalizedProperties.proxy(ProxyInterface.class);

            /**
             * Setup with caching.
             */
            externalizedPropertiesWithCaching = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(propertySource))
                .withCaching()
                .withCacheDuration(Duration.ofHours(3))
                .build();

            proxyInterfaceWithCaching = 
                externalizedPropertiesWithCaching.proxy(ProxyInterface.class);

            /**
             * Setup with proxy invocation caching.
             */
            ExternalizedProperties externalizedPropertiesWithInvocationCaching = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(new MapPropertyResolver(propertySource))
                    .withProxyInvocationCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceWithInvocationCaching = 
                externalizedPropertiesWithInvocationCaching.proxy(ProxyInterface.class);

            /**
             * Setup with proxy eager loading.
             */
            ExternalizedProperties externalizedPropertiesWithEagerLoading = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(new MapPropertyResolver(propertySource))
                    .withProxyEagerLoading()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceWithEagerLoading = 
                externalizedPropertiesWithEagerLoading.proxy(ProxyInterface.class);
        }
    }

    /**
     * Benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class ResolutionBenchmarksAvgt extends ResolutionBenchmarks {}

    /**
     * Benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class ResolutionBenchmarksThrpt extends ResolutionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(Threads.MAX)
    public static class ResolutionBenchmarksAvgtMultiThreaded extends ResolutionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(Threads.MAX)
    public static class ResolutionBenchmarksThrptMultiThreaded extends ResolutionBenchmarks {}

    /**
     * Benchmark retrieval of items from a map.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineConcurrentMap(BenchmarkState state) {
        return state.baselineMap.get("test");
    }

    /**
     * Benchmark retrieval of items from system properties.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineSystemProperty(BenchmarkState state) {
        return System.getProperty("test");
    }

    /**
     * Benchmark retrieval of items from environment variables.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineEnvVar(BenchmarkState state) {
        return System.getenv("PATH");
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<String> resolveProperty(BenchmarkState state) {
        return state.externalizedProperties.resolveProperty("test");
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * while caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<String> resolvePropertyWithCaching(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.resolveProperty("test");
    }

    /**
     * Benchmark resolution of properties from a proxy interface.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterface(BenchmarkState state) {
        return state.proxyInterface.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface while caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceWithCaching(BenchmarkState state) {
        return state.proxyInterfaceWithCaching.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface while proxy 
     * invocation caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceWithInvocationCaching(BenchmarkState state) {
        return state.proxyInterfaceWithInvocationCaching.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface  with proxy
     * eager loading enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceWithEagerLoading(BenchmarkState state) {
        return state.proxyInterfaceWithEagerLoading.test();
    }
}
