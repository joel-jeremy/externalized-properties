package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapPropertyResolverTests {
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
    }

    @Nested
    class ResolveMethodSingleProperty {
        @Test
        @DisplayName("should throw when property name argument is null or empty")
        public void validationTest1() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve((String)null)
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve("")
            );
        }

        @Test
        @DisplayName("should resolve values from the given map")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name", "property.value");
            
            MapPropertyResolver resolver = resolverToTest(map);
            Optional<ResolvedProperty> result = resolver.resolve(
                "property.name"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                map.get("property.name"), 
                result.get().value()    
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the given map"
        )
        public void test2() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());
            Optional<ResolvedProperty> result = resolver.resolve(
                "nonexisting.property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
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

            Optional<ResolvedProperty> result = 
                resolver.resolve("property.unresolvedhandler");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler"), 
                result.get().value()    
            );
        }
    }

    @Nested
    class ResolveMethodMultipleProperties {
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
        @DisplayName(
            "should throw when property names collection contains any null or empty values"
        )
        public void validationTest3() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());

            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", null))
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(Arrays.asList("property", ""))
            );
        }

        @Test
        @DisplayName("should throw when property names varargs contain any null or empty values")
        public void validationTest4() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", null })
            );
            
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolver.resolve(new String[] { "property", "" })
            );
        }

        @Test
        @DisplayName("should resolve values from the given map")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name1", "property.value1");
            map.put("property.name2", "property.value2");
            
            MapPropertyResolver resolver = resolverToTest(map);
            ExternalizedPropertyResolverResult result = resolver.resolve(
                "property.name1",
                "property.name2"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                map.get("property.name1"), 
                result.findRequiredPropertyValue("property.name1")
            );

            assertEquals(
                map.get("property.name2"), 
                result.findRequiredPropertyValue("property.name2")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given map"
        )
        public void test2() {
            MapPropertyResolver resolver = resolverToTest(Collections.emptyMap());
            ExternalizedPropertyResolverResult result = resolver.resolve(
                "nonexisting.property1",
                "nonexisting.property2"
            );
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property1"));
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property2"));
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
                resolver.resolve(
                    "property.unresolvedhandler1",
                    "property.unresolvedhandler2"
                );
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());

            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler1"), 
                result.findRequiredPropertyValue("property.unresolvedhandler1")
            );

            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler2"), 
                result.findRequiredPropertyValue("property.unresolvedhandler2")
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
}
