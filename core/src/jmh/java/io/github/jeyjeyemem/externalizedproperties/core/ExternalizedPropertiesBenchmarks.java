package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.handlers.PrimitiveConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
public class ExternalizedPropertiesBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private Map<String, String> propertySource;
        private ExternalizedProperties externalizedProperties;
        private ProxyInterface proxyInterface;
        private ProxyInterface proxyInterfaceWithCaching;
        private ProxyInterface proxyInterfaceWithInvocationCaching;
        private ProxyInterface proxyInterfaceWithEagerLoading;
        private ExternalizedProperties externalizedPropertiesWithCaching;

        @Setup
        public void setup() {
            propertySource = new ConcurrentHashMap<>();
            propertySource.put("test", "test");
            propertySource.put("testInt", "1");
    
            /**
             * Basic setup. No caching.
             */
            externalizedProperties = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(propertySource))
                .conversionHandlers(new PrimitiveConversionHandler())
                .build();

            proxyInterface = externalizedProperties.proxy(ProxyInterface.class);

            /**
             * Setup with caching.
             */
            externalizedPropertiesWithCaching = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapPropertyResolver(propertySource))
                .withCaching()
                .withCacheDuration(Duration.ofHours(3))
                .conversionHandlers(new PrimitiveConversionHandler())
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
                    .withCacheDuration(Duration.ofHours(3))
                    .conversionHandlers(new PrimitiveConversionHandler())
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
                    .withCacheDuration(Duration.ofHours(3))
                    .conversionHandlers(new PrimitiveConversionHandler())
                    .build();

            proxyInterfaceWithEagerLoading = 
                externalizedPropertiesWithEagerLoading.proxy(ProxyInterface.class);
        }
    }

    /**
     * Benchmark retrieval of items from a map.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String baselineMap(BenchmarkState state) {
        return state.propertySource.get("test");
    }

    /**
     * Benchmark retrieval of items from a map and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int baselineMapConversion(BenchmarkState state) {
        return Integer.parseInt(state.propertySource.get("testInt"));
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<String> direct(BenchmarkState state) {
        return state.externalizedProperties.resolveProperty("test");
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<Integer> directConversion(BenchmarkState state) {
        return state.externalizedProperties.resolveProperty("testInt", Integer.class);
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * while caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<String> directWithCaching(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.resolveProperty("test");
    }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer while caching enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public Optional<Integer> directWithCachingConversion(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.resolveProperty("testInt", Integer.class);
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
     * Benchmark resolution of properties from a proxy interface and conversion to int.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceConversion(BenchmarkState state) {
        return state.proxyInterface.testInt();
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
     * Benchmark resolution of properties from a proxy interface and conversion to int 
     * while caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithCachingConversion(BenchmarkState state) {
        return state.proxyInterfaceWithCaching.testInt();
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
     * Benchmark resolution of properties from a proxy interface and conversion to int 
     * while proxy invocation caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithInvocationCachingConversion(BenchmarkState state) {
        return state.proxyInterfaceWithInvocationCaching.testInt();
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

    /**
     * Benchmark resolution of properties from a proxy interface and conversion to int 
     * with proxy eager loading is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public int proxyInterfaceWithEagerLoadingConversion(BenchmarkState state) {
        return state.proxyInterfaceWithEagerLoading.testInt();
    }

    /**
     * Benchmark variable expansion.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String variableExpansion(BenchmarkState state) {
        return state.externalizedProperties.expandVariables("${test}");
    }

    /**
     * Benchmark variable expansion while caching is enabled.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String variableExpansionWithCaching(BenchmarkState state) {
        return state.externalizedPropertiesWithCaching.expandVariables("${test}");
    }
}
