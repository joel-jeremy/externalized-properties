package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapPropertyResolverTests {
    private static final Properties EMPTY_PROPERTIES = new Properties();

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when property source map argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapPropertyResolver((Map<String, String>)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapPropertyResolver(Collections.emptyMap(), null)
            );
        }

        @Test
        @DisplayName("should throw when properties argument is null")
        public void propertiesTest1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapPropertyResolver((Properties)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null")
        public void propertiesTest2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapPropertyResolver(EMPTY_PROPERTIES, null)
            );
        }

        @Test
        @DisplayName("should ignore properties with non-String keys or values")
        public void propertiesTest3() {
            Properties props = new Properties();
            props.put("property.nonstring", 123);
            props.put(123, "property.nonstring.key");
            props.put("property.name", "property.value");

            MapPropertyResolver resolver = resolverToTest(props);
            ExternalizedPropertyResolverResult result = 
                resolver.resolve("property.nonstring", "property.name");

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("property.nonstring"));

            assertEquals(
                props.get("property.name"), 
                result.findResolvedProperty("property.name")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty")
        public void validationTest1() {
        MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((Collection<String>)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Collections.emptyList())
            );
        }

        @Test
        @DisplayName("should throw when property names varargs argument is null or empty")
        public void validationTest2() {
        MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String[])null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[0])
            );
        }

        @Test
        @DisplayName("should resolve values from the given map")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name", "property.value");
            
            MapPropertyResolver resolver = resolverToTest(map);
            ExternalizedPropertyResolverResult result = resolver.resolve("property.name");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                map.get("property.name"), 
                result.findResolvedProperty("property.name")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given map"
        )
        public void test2() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());
            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.property");
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property"));
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given map"
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapPropertyResolver resolver = resolverToTest(
                Collections.emptyMap(),
                unresolvedPropertyHandler
            );

            ExternalizedPropertyResolverResult result = 
                resolver.resolve("property.unresolvedhandler");
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());
            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler"), 
                result.findResolvedProperty("property.unresolvedhandler")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName("should resolve values from the given properties")
        public void propertiesTest1() {
            Properties props = new Properties();
            props.setProperty("property.name", "property.value");
            
            MapPropertyResolver resolver = resolverToTest(props);
            ExternalizedPropertyResolverResult result = resolver.resolve("property.name");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                props.getProperty("property.name"), 
                result.findResolvedProperty("property.name")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given map"
        )
        public void propertiesTest2() {
            MapPropertyResolver resolver = resolverToTest(EMPTY_PROPERTIES);
            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.property");
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property"));
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given properties"
        )
        public void propertiesTest3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapPropertyResolver resolver = resolverToTest(
                EMPTY_PROPERTIES,
                unresolvedPropertyHandler
            );

            ExternalizedPropertyResolverResult result = 
                resolver.resolve("property.unresolvedhandler");
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());
            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler"), 
                result.findResolvedProperty("property.unresolvedhandler")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }
    }

    private MapPropertyResolver resolverToTest(
            Map<String, String> map
    ) {
        return new MapPropertyResolver(map);
    }

    private MapPropertyResolver resolverToTest(
            Map<String, String> map,
            Function<String, String> unresolverPropertyHandler
    ) {
        return new MapPropertyResolver(map, unresolverPropertyHandler);
    }

    private MapPropertyResolver resolverToTest(
            Properties properties
    ) {
        return new MapPropertyResolver(properties);
    }

    private MapPropertyResolver resolverToTest(
            Properties properties,
            Function<String, String> unresolverPropertyHandler
    ) {
        return new MapPropertyResolver(properties, unresolverPropertyHandler);
    }
}
