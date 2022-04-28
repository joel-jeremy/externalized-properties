package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapResolver;
import io.github.jeyjeyemem.externalizedproperties.core.variableexpansion.NoOpVariableExpander;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark property resolution via proxy or directly from
 * {@link ExternalizedProperties}.
 */
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public abstract class ResolutionBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private String key;
        private String pathKey;
        private Map<String, String> baselineMap;
        private ResolutionProxyInterface proxyInterface;
        private ResolutionProxyInterface proxyInterfaceNoVariableExpansion;
        private ResolutionProxyInterface proxyInterfaceWithInvocationCaching;
        private ResolutionProxyInterface proxyInterfaceNoVariableExpansionWithInvocationCaching;
        private ResolutionProxyInterface proxyInterfaceWithEagerLoading;
        private ResolutionProxyInterface proxyInterfaceNoVariableExpansionWithEagerLoading;

        @Setup
        public void setup() throws NoSuchMethodException, SecurityException {
            key = "test";
            pathKey = "PATH";

            // Use PATH value for all as we cannot easily set env variable programatically.
            String value = System.getenv(pathKey);

            baselineMap = new ConcurrentHashMap<>();
            baselineMap.put(key, value);

            Map<String, String> propertySource = new ConcurrentHashMap<>();
            propertySource.put(key, value);

            System.setProperty(key, value);

            /**
             * Setup with no proxy caching.
             */
            ExternalizedProperties externalizedProperties = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .build();
                
            /**
             * Setup with no variable expansion and no proxy caching.
             */
            ExternalizedProperties externalizedPropertiesNoVariableExpansion = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .variableExpander(NoOpVariableExpander.provider())
                    .build();

            proxyInterface = 
                externalizedProperties.proxy(
                    ResolutionProxyInterface.class
                );
            
            proxyInterfaceNoVariableExpansion = 
                externalizedPropertiesNoVariableExpansion.proxy(
                    ResolutionProxyInterface.class
                );

            /**
             * Setup with proxy invocation caching.
             */
            ExternalizedProperties withInvocationCaching = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .withProxyInvocationCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceWithInvocationCaching = 
                withInvocationCaching.proxy(
                    ResolutionProxyInterface.class
                );

            /**
             * Setup no variable expansion with proxy invocation caching.
             */
            ExternalizedProperties noVariableExpansionWithInvocationCaching = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .variableExpander(NoOpVariableExpander.provider())
                    .withProxyInvocationCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceNoVariableExpansionWithInvocationCaching = 
                noVariableExpansionWithInvocationCaching.proxy(
                    ResolutionProxyInterface.class
                );
            
            /**
             * Setup with proxy eager loading.
             */
            ExternalizedProperties withEagerLoading = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .withProxyEagerLoading()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceWithEagerLoading = 
                withEagerLoading.proxy(ResolutionProxyInterface.class);

            /**
             * Setup no variable expansion with proxy eager loading.
             */
            ExternalizedProperties noVariableExpansionWithEagerLoading = 
                ExternalizedProperties.builder()
                    .resolvers(MapResolver.provider(propertySource))
                    .variableExpander(NoOpVariableExpander.provider())
                    .withProxyInvocationCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .build();

            proxyInterfaceNoVariableExpansionWithEagerLoading = 
                noVariableExpansionWithEagerLoading.proxy(
                    ResolutionProxyInterface.class
                );
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
     * Benchmark retrieval of items from a concurrent map.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineConcurrentMap(BenchmarkState state) {
        return state.baselineMap.get(state.key);
    }

    /**
     * Benchmark retrieval of items from system properties.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineSystemProperty(BenchmarkState state) {
        return System.getProperty(state.key);
    }

    /**
     * Benchmark retrieval of items from environment variables.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineEnvVar(BenchmarkState state) {
        return System.getenv(state.pathKey);
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
     * Benchmark resolution of properties from a proxy interface with no variable 
     * expansion.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceNoVariableExpansion(BenchmarkState state) {
        return state.proxyInterfaceNoVariableExpansion.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface with proxy
     * eager loading enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceWithEagerLoading(BenchmarkState state) {
        return state.proxyInterfaceWithEagerLoading.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface with no variable expansion 
     * and with proxy eager loading enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceNoVariableExpansionWithEagerLoading(BenchmarkState state) {
        return state.proxyInterfaceNoVariableExpansionWithEagerLoading.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface with proxy 
     * invocation caching enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceWithInvocationCaching(BenchmarkState state) {
        return state.proxyInterfaceWithInvocationCaching.test();
    }

    /**
     * Benchmark resolution of properties from a proxy interface with no variable expansion and 
     * proxy invocation caching enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String proxyInterfaceNoVariableExpansionWithInvocationCaching(BenchmarkState state) {
        return state.proxyInterfaceNoVariableExpansionWithInvocationCaching.test();
    }
}
