package io.github.joeljeremy7.externalizedproperties.core;

import io.github.joeljeremy7.externalizedproperties.core.resolvers.MapResolver;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.PatternVariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark {@link ExternalizedProperties}' variable expansion API.
 */
@Warmup(time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS)
@Fork(3)
public abstract class VariableExpansionBenchmarks {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private VariableExpansionProxyInterface proxyWithSimpleVariableExpander;
        private VariableExpansionProxyInterface proxyWithPatternVariableExpander;

        @Setup
        public void setup() {
            Map<String, String> propertySource = Collections.singletonMap("test", "test");
    
            ExternalizedProperties withSimpleVariableExpander = 
                ExternalizedProperties.builder()
                    .resolvers(new MapResolver(propertySource))
                    .variableExpander(new SimpleVariableExpander())
                    .build();

            proxyWithSimpleVariableExpander = 
                withSimpleVariableExpander.initialize(VariableExpansionProxyInterface.class);

            ExternalizedProperties withPatternVariableExpander = 
                ExternalizedProperties.builder()
                    .resolvers(new MapResolver(propertySource))
                    .variableExpander(new PatternVariableExpander())
                    .build();

            proxyWithPatternVariableExpander = 
                withPatternVariableExpander.initialize(VariableExpansionProxyInterface.class);
        }
    }

    /**
     * Benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public static class VariableExpansionBenchmarksAvgt extends VariableExpansionBenchmarks {}

    /**
     * Benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public static class VariableExpansionBenchmarksThrpt extends VariableExpansionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure average time.
     */
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Threads(Threads.MAX)
    public static class VariableExpansionBenchmarksAvgtMultiThreaded 
        extends VariableExpansionBenchmarks {}

    /**
     * Multi-threaded benchmarks that measure throughput.
     */
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Threads(Threads.MAX)
    public static class VariableExpansionBenchmarksThrptMultiThreaded 
        extends VariableExpansionBenchmarks {}

    /**
     * Benchmark simple variable expander.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String simple(BenchmarkState state) {
        return state.proxyWithSimpleVariableExpander.test();
    }

    /**
     * Benchmark pattern variable expander.
     * 
     * @param state The benchmark state.
     * @return For you, blackhole.
     */
    @Benchmark
    public String pattern(BenchmarkState state) {
        return state.proxyWithPatternVariableExpander.test();
    }
}
