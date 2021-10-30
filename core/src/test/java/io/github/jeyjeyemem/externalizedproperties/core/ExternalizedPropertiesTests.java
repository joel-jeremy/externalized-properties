package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentVariablePropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VoidReturnTypeProxyInterface;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalizedPropertiesTests {
    private static final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor();

    @AfterAll
    public static void cleanup() {
        expiryScheduler.shutdown();
    }

    @Nested
    class Builder {
        @Test
        @DisplayName("should throw when externalizedPropertyResolvers collection argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .resolvers((Collection<ExternalizedPropertyResolver>)null)
            );
        }

        @Test
        @DisplayName("should throw when externalizedPropertyResolvers varargs argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .resolvers((ExternalizedPropertyResolver[])null)
            );
        }

        @Test
        @DisplayName("should throw when resolvedPropertyConverters collection argument is null")
        public void test4() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .conversionHandlers((Collection<ResolvedPropertyConversionHandler<?>>)null)
            );
        }

        @Test
        @DisplayName("should throw when resolvedPropertyConverters varargs argument is null")
        public void test5() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .conversionHandlers((ResolvedPropertyConversionHandler<?>[])null)
            );
        }

        @Test
        @DisplayName("should throw when cache item lifetime argument is null")
        public void test6() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder().enableCachingResolver(null, expiryScheduler)
            );
        }

        @Test
        @DisplayName("should throw when expiry scheduler is null")
        public void test7() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .enableCachingResolver(Duration.ofMinutes(5), null)
            );
        }

        @Test
        @DisplayName("should throw when cache strategy is null")
        public void test8() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .enableCachingResolver(Duration.ofMinutes(5), expiryScheduler, null)
            );
        }

        @Test
        @DisplayName("should throw when on build when there are no resolvers")
        public void test9() {
            assertThrows(
                IllegalStateException.class,
                () -> ExternalizedProperties.builder().build()
            );
        }

        @Test
        @DisplayName("should allow multiple resolvers")
        public void test10() {
            ExternalizedProperties ep = ExternalizedProperties.builder()
                .resolvers(
                    new SystemPropertyResolver(),
                    new EnvironmentVariablePropertyResolver()
                )
                .build();

            JavaPropertiesProxyInterface proxyInterface = 
                ep.initialize(JavaPropertiesProxyInterface.class);

            // Resolved from system properties.
            assertEquals(
                System.getProperty("java.version"), 
                proxyInterface.javaVersion()
            );

            // Resolved from environment variables.
            assertEquals(
                System.getenv("JAVA_HOME"), 
                proxyInterface.javaHomeEnv()
            );
        }
    }

    @Nested
    class InitializeMethod {
        @Test
        @DisplayName("should not return null")
        public void validationTest1() {
            ExternalizedProperties externalizedProperties = externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = externalizedProperties.initialize(BasicProxyInterface.class);

            assertNotNull(proxyInterface);
        }

        @Test
        @DisplayName("should throw when proxy interface is null")
        public void validationTest2() {
            ExternalizedProperties externalizedProperties = externalizedProperties(Collections.emptyMap());
            
            assertThrows(IllegalArgumentException.class, () -> externalizedProperties.initialize(null));
        }

        @Test
        @DisplayName("should throw when proxy interface is not an interface")
        public void validationTest3() {
            ExternalizedProperties externalizedProperties = externalizedProperties(Collections.emptyMap());
            
            assertThrows(IllegalArgumentException.class, () -> 
                externalizedProperties.initialize(ExternalizedPropertiesTests.class));
        }

        @Test
        @DisplayName("should not allow proxy interface methods with void return type")
        public void validationTest4() {
            ExternalizedProperties externalizedProperties = externalizedProperties(Collections.emptyMap());
            
            assertThrows(IllegalArgumentException.class, () -> 
                externalizedProperties.initialize(VoidReturnTypeProxyInterface.class));
        }
    }

    private ExternalizedProperties externalizedProperties(
            Map<String, String> propertySource,
            ResolvedPropertyConversionHandler<?>... resolvedPropertyConversionHandlers
    ) {
        return externalizedProperties(
            Arrays.asList(new MapPropertyResolver(propertySource)),
            Arrays.asList(resolvedPropertyConversionHandlers)
        );
    }

    private ExternalizedProperties externalizedProperties(
            Collection<ExternalizedPropertyResolver> resolvers,
            Collection<ResolvedPropertyConversionHandler<?>> resolvedPropertyConversionHandlers
    ) {
        ExternalizedProperties.Builder builder = 
            ExternalizedProperties.builder()
                .resolvers(resolvers)
                .conversionHandlers(resolvedPropertyConversionHandlers)
                .enableCachingResolver(Duration.ofMinutes(5), expiryScheduler);
        
        if (resolvedPropertyConversionHandlers.size() == 0) {
            builder.enableDefaultConversionHandlers();
        }

        return builder.build();
    }
}
