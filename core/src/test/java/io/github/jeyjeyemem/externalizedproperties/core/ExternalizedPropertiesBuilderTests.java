package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.StubResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertiesBuilderTests {
    @Nested
    class NewBuilderMethod {
        @Test
        @DisplayName("should not return null")
        public void test1() {
            assertNotNull(ExternalizedPropertiesBuilder.newBuilder());
        }
    }

    @Nested
    class ResolversMethod {
        @Test
        @DisplayName("should throw when resolvers collection argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers((Collection<Resolver>)null)
            );
        }

        @Test
        @DisplayName("should throw when resolvers varargs argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers((Resolver[])null)
            );
        }
    }

    @Nested
    class ConversionHandlersMethod {
        @Test
        @DisplayName("should throw when converters collection argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .converters((Collection<Converter<?>>)null)
            );
        }
    
        @Test
        @DisplayName("should throw when converters varargs argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .converters((Converter<?>[])null)
            );
        }
    }

    @Nested
    class ProcessorsMethod {
        @Test
        @DisplayName("should throw when processors collection argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .processors((Collection<Processor>)null)
            );
        }
    
        @Test
        @DisplayName("should throw when processors varargs argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .processors((Processor[])null)
            );
        }
    }

    @Nested
    class VariableExpanderMethod {
        @Test
        @DisplayName("should throw when variable expander factory argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .variableExpander(null)
            );
        }
    }

    @Nested
    class BuildMethod {
        @Test
        @DisplayName("should throw on build when there are no resolvers")
        public void test1() {
            assertThrows(
                IllegalStateException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder().build()
            );
        }

        @Test
        @DisplayName(
            "should throw on build when variable expander factory returns null"
        )
        public void test2() {
            assertThrows(
                IllegalStateException.class,
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .variableExpander(resolver -> null)
                    .build()
            );
        }
    }

    @Nested
    class WithDefaultResolversMethod {
        @Test
        @DisplayName("should register default resolvers")
        public void test1() {
            // Default resolvers include:
            // - System property resolver
            // - Environment variable resolver
            ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaultResolvers()
                .build();

            testDefaultResolvers(ep);
        }
    }

    @Nested
    class WithDefaultConversionHandlersMethod {
        @Test
        @DisplayName("should register default conversion handlers")
        public void test1() {
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
            ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(new MapResolver(map))
                .withDefaultConverters()
                .build();
    
            testDefaultConversionHandlers(ep);
        }
    }

    @Nested
    class WithDefaultsMethod {
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
            ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
                .withDefaults()
                .build();
    
            testDefaultResolvers(ep);
            testDefaultConversionHandlers(ep);
        }
    }

    @Nested
    class WithCacheDurationMethod {
        @Test
        @DisplayName("should throw when cache duration argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ExternalizedPropertiesBuilder.newBuilder().withCacheDuration(null)
            );
        }

        @Test
        @DisplayName("should throw when cache duration argument is null")
        public void test2() {
            assertDoesNotThrow(
                () -> ExternalizedPropertiesBuilder.newBuilder()
                    .withCacheDuration(Duration.ofMinutes(1))
            );
        }
    }

    @Nested
    class WithProxyEagerLoadingMethod {
        @Test
        @DisplayName("should enable proxy eager loading")
        public void test1() {
            StubResolver resolver = new StubResolver();

            ExternalizedProperties externalizedProperties = 
                ExternalizedPropertiesBuilder.newBuilder()
                    .resolvers(resolver)
                    .withProxyEagerLoading()
                    .build();

            JavaPropertiesProxyInterface proxy = 
                externalizedProperties.proxy(JavaPropertiesProxyInterface.class);

            assertNotNull(proxy);

            // Properties were already eagerly resolved via resolver.
            assertTrue(resolver.resolvedPropertyNames().contains("java.version"));
            assertTrue(resolver.resolvedPropertyNames().contains("PATH"));
        }
    }

    private void testDefaultResolvers(ExternalizedProperties ep) {
        JavaPropertiesProxyInterface proxyInterface = 
            ep.proxy(JavaPropertiesProxyInterface.class);

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
            ep.proxy(PrimitiveProxyInterface.class);
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
            ep.proxy(ListProxyInterface.class);
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
            ep.proxy(ArrayProxyInterface.class);
        assertArrayEquals(
            new String[] { "a", "b", "c" }, 
            arrayProxy.arrayProperty()
        );

        // Array conversion
        OptionalProxyInterface optionalProxy = 
            ep.proxy(OptionalProxyInterface.class);
        Optional<String> prop = optionalProxy.optionalProperty();
        assertTrue(prop.isPresent());
        assertEquals("optional-value", prop.get());
    }
}
