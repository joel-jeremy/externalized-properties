package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.EnumConverter;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.converters.PrimitiveConverter;
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
        private ProxyInterface proxyInterface;
        private ProxyInterface proxyInterfaceWithCaching;
        private ProxyInterface proxyInterfaceWithMultipleConverter;

        @Setup
        public void setup() throws NoSuchMethodException, SecurityException {
            /**
             * Basic setup. No caching.
             */
            ExternalizedProperties externalizedProperties =     
                ExternalizedProperties.builder()
                    .resolvers(new MapResolver(
                        Collections.singletonMap("testInt", "1")
                    ))
                    .converters(new PrimitiveConverter())
                    .build();

            proxyInterface = externalizedProperties.proxy(ProxyInterface.class);

            // context = new ConversionContext(
            //     converter, 
            //     new ProxyMethod(
            //         proxyInterface,
            //         proxyInterface.getClass().getDeclaredMethod("test"),
            //         externalizedProperties,
            //         new MethodHandleFactory()
            //     ), 
            //     "1"
            // );

            /**
             * Setup with caching.
             */
            ExternalizedProperties externalizedPropertiesWithCaching = 
                ExternalizedProperties.builder()
                    .resolvers(new MapResolver(
                        Collections.singletonMap("testInt", "1")
                    ))
                    .withResolverCaching()
                    .withCacheDuration(Duration.ofHours(24))
                    .converters(new PrimitiveConverter())
                    .build();

            proxyInterfaceWithCaching = 
                externalizedPropertiesWithCaching.proxy(ProxyInterface.class);

            /**
             * Basic setup. No caching.
             */
            ExternalizedProperties externalizedPropertiesWithMultipleConverters = 
                ExternalizedProperties.builder()
                    .resolvers(new MapResolver(
                        Collections.singletonMap("testInt", "1")
                    ))
                    // Add a bunch more converters.
                    .converters(
                        Stream.generate(() -> new EnumConverter())
                            .limit(10)
                            .toArray(Converter<?>[]::new)
                    )
                    .converters(new PrimitiveConverter())
                    .build();

            proxyInterfaceWithMultipleConverter = 
                externalizedPropertiesWithMultipleConverters.proxy(
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
    // @Benchmark
    // public Object resolveProperty(BenchmarkState state) {
    //     return state.externalizedProperties.convert(state.context).value();
    // }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    // @Benchmark
    // public Optional<Integer> resolvePropertyWithCaching(BenchmarkState state) {
    //     return state.externalizedPropertiesWithCaching.resolveProperty("testInt", Integer.class);
    // }

    /**
     * Benchmark resolution of properties directly from ExternalizedProperties
     * and conversion to integer.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    // @Benchmark
    // public Optional<Integer> resolvePropertyWithMultipleConverters(
    //         BenchmarkState state
    // ) {
    //     return state.externalizedPropertiesWithMultipleConverters.resolveProperty(
    //         "testInt", 
    //         Integer.class
    //     );
    // }

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
    public int proxyInterfaceWithMultipleConverter(BenchmarkState state) {
        return state.proxyInterfaceWithMultipleConverter.testInt();
    }

    // private static class IntegerConverter implements Converter<Integer> {
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
