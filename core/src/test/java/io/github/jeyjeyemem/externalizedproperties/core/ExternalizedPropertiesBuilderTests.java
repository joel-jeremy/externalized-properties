package io.github.jeyjeyemem.externalizedproperties.core;

import io.github.jeyjeyemem.externalizedproperties.core.conversion.ConversionHandler;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.EnvironmentPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.MapPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ArrayProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.BasicProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.JavaPropertiesProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.ListProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.OptionalProxyInterface;
import io.github.jeyjeyemem.externalizedproperties.core.testentities.proxy.PrimitiveProxyInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalizedPropertiesBuilderTests {
    @Test
    @DisplayName("should throw when externalized property resolvers collection argument is null")
    public void test1() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .resolvers((Collection<ExternalizedPropertyResolver>)null)
        );
    }

    @Test
    @DisplayName("should throw when externalized property resolvers varargs argument is null")
    public void test2() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .resolvers((ExternalizedPropertyResolver[])null)
        );
    }

    @Test
    @DisplayName("should throw when converters collection argument is null")
    public void test3() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .conversionHandlers((Collection<ConversionHandler<?>>)null)
        );
    }

    @Test
    @DisplayName("should throw when converters varargs argument is null")
    public void test4() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .conversionHandlers((ConversionHandler<?>[])null)
        );
    }

    @Test
    @DisplayName("should throw when cache item lifetime argument is null")
    public void test5() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .withCaching(null)
        );
    }

    @Test
    @DisplayName("should throw when invocation cache item lifetime argument is null")
    public void test6() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .withInvocationCaching(null)
        );
    }

    @Test
    @DisplayName("should throw when eager loading cache item lifetime is null")
    public void test7() {
        assertThrows(
            IllegalArgumentException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder()
                .withEagerLoading(null)
        );
    }

    @Test
    @DisplayName("should throw when on build when there are no resolvers")
    public void test8() {
        assertThrows(
            IllegalStateException.class,
            () -> ExternalizedPropertiesBuilder.newBuilder().build()
        );
    }

    @Test
    @DisplayName("should allow multiple resolvers")
    public void test9() {
        Map<String, String> map = new HashMap<>();
        map.put("property", "value");

        ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
            .resolvers(
                new SystemPropertyResolver(),
                new EnvironmentPropertyResolver(),
                new MapPropertyResolver(map)
            )
            .build();

        JavaPropertiesProxyInterface javaProxyInterface = 
            ep.proxy(JavaPropertiesProxyInterface.class);

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
            ep.proxy(BasicProxyInterface.class);

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
        ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
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
        ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
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
        ExternalizedProperties ep = ExternalizedPropertiesBuilder.newBuilder()
            .withDefaults()
            .build();

        testDefaultResolvers(ep);
        testDefaultConversionHandlers(ep);
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
