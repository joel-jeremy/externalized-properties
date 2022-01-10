package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapResolverTests {

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when property source map argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapResolver((Map<String, String>)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new MapResolver(new HashMap<>(), null)
            );
        }
    }

    @Nested
    class ResolveMethod {

        @Test
        @DisplayName("should throw when property name argument is null or empty.")
        public void validationTest1() {
            MapResolver resolver = resolverToTest();

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
        @DisplayName("should resolve values from the given map.")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name", "property.value");
            
            MapResolver resolver = resolverToTest(map);
            Optional<String> result = resolver.resolve(
                "property.name"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                map.get("property.name"), 
                result.get()   
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the given map."
        )
        public void test2() {
            MapResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "nonexisting.property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given map."
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapResolver resolver = resolverToTest(
                new HashMap<>(),
                unresolvedPropertyHandler
            );

            Optional<String> result = 
                resolver.resolve("property.unresolvedhandler");
            
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler"), 
                result.get()    
            );
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should throw when property names varargs argument is null or empty.")
        public void validationTest1() {
            MapResolver resolver = resolverToTest();

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
            "should throw when property names varargs contain any null or empty values."
        )
        public void validationTest2() {
            MapResolver resolver = resolverToTest();
            
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
        @DisplayName("should resolve values from the given map.")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name1", "property.value1");
            map.put("property.name2", "property.value2");

            String[] propertiesToResolve = new String[] {
                "property.name1",
                "property.name2"
            };
            
            MapResolver resolver = resolverToTest(map);
            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    map.get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given map."
        )
        public void test2() {
            MapResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "nonexisting.property1",
                "nonexisting.property2"
            };

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given map."
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapResolver resolver = resolverToTest(
                new HashMap<>(),
                unresolvedPropertyHandler
            );

            String[] propertiesToResolve = new String[] {
                "property.unresolvedhandler1",
                "property.unresolvedhandler2"
            };

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    unresolvedPropertyHandler.apply(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should throw when property names collection argument is null or empty.")
        public void validationTest1() {
        MapResolver resolver = resolverToTest();

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
        @DisplayName(
            "should throw when property names collection contains any null or empty values."
        )
        public void validationTest2() {
            MapResolver resolver = resolverToTest();

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
        @DisplayName("should resolve values from the given map.")
        public void test1() {
            Map<String, String> map = new HashMap<>();
            map.put("property.name1", "property.value1");
            map.put("property.name2", "property.value2");

            List<String> propertiesToResolve = Arrays.asList(
                "property.name1",
                "property.name2"
            );
            
            MapResolver resolver = resolverToTest(map);
            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    map.get(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given map."
        )
        public void test2() {
            MapResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "nonexisting.property1",
                "nonexisting.property2"
            );

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertTrue(
                    result.unresolvedPropertyNames().contains(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given map."
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            MapResolver resolver = resolverToTest(
                new HashMap<>(),
                unresolvedPropertyHandler
            );

            List<String> propertiesToResolve = Arrays.asList(
                "property.unresolvedhandler1",
                "property.unresolvedhandler2"
            );

            ResolverResult result = resolver.resolve(
                propertiesToResolve
            );
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    unresolvedPropertyHandler.apply(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }
    }

    private MapResolver resolverToTest() {
        return new MapResolver(new HashMap<>());
    }

    private MapResolver resolverToTest(Map<String, String> map) {
        return new MapResolver(map);
    }

    private MapResolver resolverToTest(
            Map<String, String> map,
            Function<String, String> unresolverPropertyHandler
    ) {
        return new MapResolver(map, unresolverPropertyHandler);
    }
}
