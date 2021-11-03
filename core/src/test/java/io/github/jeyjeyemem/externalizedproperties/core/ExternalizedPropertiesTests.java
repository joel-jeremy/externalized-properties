package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentVariablePropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.CachingPropertyResolver.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.VoidReturnTypeProxyInterface;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        @DisplayName("should throw when externalized property resolvers collection argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .resolvers((Collection<ExternalizedPropertyResolver>)null)
            );
        }

        @Test
        @DisplayName("should throw when externalized property resolvers varargs argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .resolvers((ExternalizedPropertyResolver[])null)
            );
        }

        @Test
        @DisplayName("should throw when resolved property converters collection argument is null")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .conversionHandlers((Collection<ResolvedPropertyConversionHandler<?>>)null)
            );
        }

        @Test
        @DisplayName("should throw when resolved property converters varargs argument is null")
        public void test4() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .conversionHandlers((ResolvedPropertyConversionHandler<?>[])null)
            );
        }

        @Test
        @DisplayName("should throw when cache item lifetime argument is null")
        public void test5() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .withCachingResolver(null, expiryScheduler)
            );
        }

        @Test
        @DisplayName("should throw when expiry scheduler is null")
        public void test6() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .withCachingResolver(Duration.ofMinutes(5), null)
            );
        }

        @Test
        @DisplayName("should throw when cache strategy is null")
        public void test7() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedProperties.builder()
                    .withCachingResolver(Duration.ofMinutes(5), expiryScheduler, null)
            );
        }

        @Test
        @DisplayName("should throw when on build when there are no resolvers")
        public void test8() {
            assertThrows(
                IllegalStateException.class,
                () -> ExternalizedProperties.builder().build()
            );
        }

        @Test
        @DisplayName("should allow multiple resolvers")
        public void test9() {
            Map<String, String> map = new HashMap<>();
            map.put("property", "value");

            ExternalizedProperties ep = ExternalizedProperties.builder()
                .resolvers(
                    new SystemPropertyResolver(),
                    new EnvironmentVariablePropertyResolver(),
                    new MapPropertyResolver(map)
                )
                .build();

            JavaPropertiesProxyInterface javaProxyInterface = 
                ep.initialize(JavaPropertiesProxyInterface.class);

            // Resolved from system properties.
            assertEquals(
                System.getProperty("java.version"), 
                javaProxyInterface.javaVersion()
            );

            // Resolved from environment variables.
            assertEquals(
                System.getenv("PATH"), 
                javaProxyInterface.pathEnv()
            );

            BasicProxyInterface basicProxyInterface = 
                ep.initialize(BasicProxyInterface.class);

            // Resolved from map resolver.
            assertEquals("value", basicProxyInterface.property());
        }

        @Test
        @DisplayName(
            "should register default resolvers"
        )
        public void test10() {
            // Default resolvers include:
            // - System property resolver
            // - Environment variable resolver
            ExternalizedProperties ep = ExternalizedProperties.builder()
                .withDefaultResolvers()
                .build();

            testDefaultResolvers(ep);
        }

        @Test
        @DisplayName(
            "should register default conversion handlers"
        )
        public void test11() {
            Map<String, String> map = new HashMap<>();
            map.put("property.integer.primitive", "1");
            map.put("property.integer.wrapper", "1");
            map.put("property.long.primitive", "2");
            map.put("property.long.wrapper", "2");
            map.put("property.double.primitive", "3.0");
            map.put("property.double.wrapper", "3.0");
            map.put("property.float.primitive", "4.0");
            map.put("property.float.wrapper", "4.0");
            map.put("property.list", "a,b,c");
            map.put("property.collection", "c,b,a");
            map.put("property.array", "a,b,c");
            map.put("property.optional", "optional-value");

            // Default conversion handler includes conversion to:
            // - Primitives
            // - Lists/Collections
            // - Arrays
            // - Optionals
            ExternalizedProperties ep = ExternalizedProperties.builder()
                .resolvers(new MapPropertyResolver(map))
                .withDefaultConversionHandlers()
                .build();

            testDefaultConversionHandlers(ep);
        }

        @Test
        @DisplayName("should register default resolvers and conversion handlers")
        public void test12() {
            // System properties.
            System.setProperty("property.integer.primitive", "1");
            System.setProperty("property.integer.wrapper", "1");
            System.setProperty("property.long.primitive", "2");
            System.setProperty("property.long.wrapper", "2");
            System.setProperty("property.double.primitive", "3.0");
            System.setProperty("property.double.wrapper", "3.0");
            System.setProperty("property.float.primitive", "4.0");
            System.setProperty("property.float.wrapper", "4.0");
            System.setProperty("property.list", "a,b,c");
            System.setProperty("property.collection", "c,b,a");
            System.setProperty("property.array", "a,b,c");
            System.setProperty("property.optional", "optional-value");

            // Default conversion handler includes conversion to:
            // - Primitives
            // - Lists/Collections
            // - Arrays
            // - Optionals
            ExternalizedProperties ep = ExternalizedProperties.builder()
                .withDefaults()
                .build();

            testDefaultResolvers(ep);
            testDefaultConversionHandlers(ep);
        }

        @Test
        @DisplayName("should use provided cache strategy")
        public void test13() {
            ConcurrentMap<String, ResolvedProperty> cache = new ConcurrentHashMap<>();
            CacheStrategy cacheStrategy = 
                new CachingPropertyResolver.ConcurrentMapCacheStrategy(cache);

            Map<String, String> map = new HashMap<>();
            map.put("property", "value");

            ExternalizedProperties ep = ExternalizedProperties.builder()
                .resolvers(new MapPropertyResolver(map))
                .withCachingResolver(Duration.ofSeconds(30), expiryScheduler, cacheStrategy)
                .build();

            BasicProxyInterface proxyInterface = ep.initialize(BasicProxyInterface.class);

            assertEquals("value", proxyInterface.property());
            // property should have been added to cache via cache strategy.
            assertTrue(cache.containsKey("property"));
        }

        private void testDefaultResolvers(ExternalizedProperties ep) {
            JavaPropertiesProxyInterface proxyInterface = 
                ep.initialize(JavaPropertiesProxyInterface.class);

            // Resolved from system properties.
            assertEquals(
                System.getProperty("java.version"), 
                proxyInterface.javaVersion()
            );

            // Resolved from environment variables.
            assertEquals(
                System.getenv("PATH"), 
                proxyInterface.pathEnv()
            );
        }

        private void testDefaultConversionHandlers(ExternalizedProperties ep) {
            // Primitive conversions
            PrimitiveProxyInterface primitiveProxy = 
                ep.initialize(PrimitiveProxyInterface.class);
            assertEquals(1, primitiveProxy.intPrimitiveProperty());
            assertEquals(1, primitiveProxy.integerWrapperProperty());
            assertEquals(2, primitiveProxy.longPrimitiveProperty());
            assertEquals(2, primitiveProxy.longWrapperProperty());
            assertEquals(3.0d, primitiveProxy.doublePrimitiveProperty());
            assertEquals(3.0d, primitiveProxy.doubleWrapperProperty());
            assertEquals(4.0f, primitiveProxy.floatPrimitiveProperty());
            assertEquals(4.0f, primitiveProxy.floatWrapperProperty());
            
            // List and Collection conversions
            ListProxyInterface listProxy = 
                ep.initialize(ListProxyInterface.class);
            assertIterableEquals(
                Arrays.asList("a", "b", "c"), 
                listProxy.listProperty()
            );
            assertIterableEquals(
                Arrays.asList("c", "b", "a"), 
                listProxy.collectionProperty()
            );

            // Array conversion
            ArrayProxyInterface arrayProxy = 
                ep.initialize(ArrayProxyInterface.class);
            assertArrayEquals(
                new String[] { "a", "b", "c" }, 
                arrayProxy.arrayProperty()
            );

            // Array conversion
            OptionalProxyInterface optionalProxy = 
                ep.initialize(OptionalProxyInterface.class);
            Optional<String> prop = optionalProxy.optionalProperty();
            assertTrue(prop.isPresent());
            assertEquals("optional-value", prop.get());
        }
    }

    @Nested
    class InitializeMethod {
        @Test
        @DisplayName("should not return null")
        public void validationTest1() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());

            BasicProxyInterface proxyInterface = 
                externalizedProperties.initialize(BasicProxyInterface.class);

            assertNotNull(proxyInterface);
        }

        @Test
        @DisplayName("should throw when proxy interface is null")
        public void validationTest2() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(null)
            );
        }

        @Test
        @DisplayName("should throw when proxy interface is not an interface")
        public void validationTest3() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(ExternalizedPropertiesTests.class)
            );
        }

        @Test
        @DisplayName("should not allow proxy interface methods with void return type")
        public void validationTest4() {
            ExternalizedProperties externalizedProperties = 
                externalizedProperties(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> externalizedProperties.initialize(VoidReturnTypeProxyInterface.class)
            );
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
                .withCachingResolver(Duration.ofMinutes(5), expiryScheduler);
        
        if (resolvedPropertyConversionHandlers.size() == 0) {
            builder.withDefaultConversionHandlers();
        }

        return builder.build();
    }
}
