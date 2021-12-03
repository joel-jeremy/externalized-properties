package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertiesPropertyResolverTests {
    private static final Properties EMPTY_PROPERTIES = new Properties();

    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when properties argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertiesPropertyResolver((Properties)null)
            );
        }

        @Test
        @DisplayName("should throw when unresolved property handler argument is null")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PropertiesPropertyResolver(EMPTY_PROPERTIES, null)
            );
        }

        @Test
        @DisplayName("should ignore properties with non-String keys or values")
        public void propertiesTest3() {
            Properties props = new Properties();
            props.put("property.nonstring", 123);
            props.put(123, "property.nonstring.key");
            props.put("property.name", "property.value");

            PropertiesPropertyResolver resolver = resolverToTest(props);
            PropertiesPropertyResolver.Result result = 
                resolver.resolve("property.nonstring", "property.name");

            assertTrue(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("property.nonstring"));

            assertEquals(
                props.get("property.name"), 
                result.findRequiredProperty("property.name")
            );
        }
    }

    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from the given properties")
        public void test1() {
            Properties props = new Properties();
            props.setProperty("property.name", "property.value");
            
            PropertiesPropertyResolver resolver = resolverToTest(props);
            Optional<String> result = resolver.resolve(
                "property.name"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                props.getProperty("property.name"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional " + 
            "when property is not found from the given properties"
        )
        public void test2() {
            PropertiesPropertyResolver resolver = resolverToTest(EMPTY_PROPERTIES);
            Optional<String> result = resolver.resolve(
                "nonexisting.property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName(
            "should invoke unresolved property handler " + 
            "when property is not found from the given properties"
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            PropertiesPropertyResolver resolver = resolverToTest(
                EMPTY_PROPERTIES,
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
        @DisplayName("should resolve property values from the given properties")
        public void test1() {
            Properties props = new Properties();
            props.setProperty("property.name1", "property.value1");
            props.setProperty("property.name2", "property.value2");

            String[] propertiesToResolve = new String[] {
                "property.name1",
                "property.name2"
            };
            
            PropertiesPropertyResolver resolver = resolverToTest(props);
            PropertiesPropertyResolver.Result result = resolver.resolve(
                propertiesToResolve
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    props.getProperty(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given properties"
        )
        public void test2() {
            PropertiesPropertyResolver resolver = resolverToTest(EMPTY_PROPERTIES);

            String[] propertiesToResolve = new String[] {
                "nonexisting.property1",
                "nonexisting.property2"
            };

            PropertiesPropertyResolver.Result result = resolver.resolve(
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
            "when property is not found from the given properties"
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            PropertiesPropertyResolver resolver = resolverToTest(
                EMPTY_PROPERTIES,
                unresolvedPropertyHandler
            );

            String[] propertiesToResolve = new String[] {
                "property.unresolvedhandler1", 
                "property.unresolvedhandler2"
            };

            PropertiesPropertyResolver.Result result = resolver.resolve(
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
        @DisplayName("should resolve property values from the given properties")
        public void test1() {
            Properties props = new Properties();
            props.setProperty("property.name1", "property.value1");
            props.setProperty("property.name2", "property.value2");

            List<String> propertiesToResolve = Arrays.asList(
                "property.name1",
                "property.name2"
            );
            
            PropertiesPropertyResolver resolver = resolverToTest(props);
            PropertiesPropertyResolver.Result result = resolver.resolve(
                propertiesToResolve
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    props.getProperty(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given properties"
        )
        public void test2() {
            PropertiesPropertyResolver resolver = resolverToTest(EMPTY_PROPERTIES);

            List<String> propertiesToResolve = Arrays.asList(
                "nonexisting.property1",
                "nonexisting.property2"
            );

            PropertiesPropertyResolver.Result result = resolver.resolve(
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
            "when property is not found from the given properties"
        )
        public void test3() {
            AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

            Function<String, String> unresolvedPropertyHandler = 
                propertyName -> {
                    unresolvedPropertyHandlerInvoked.set(true);
                    return propertyName + "-default-value";
                };

            PropertiesPropertyResolver resolver = resolverToTest(
                EMPTY_PROPERTIES,
                unresolvedPropertyHandler
            );

            List<String> propertiesToResolve = Arrays.asList(
                "property.unresolvedhandler1", 
                "property.unresolvedhandler2"
            );

            PropertiesPropertyResolver.Result result = resolver.resolve(
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

    private PropertiesPropertyResolver resolverToTest(
            Properties properties
    ) {
        return new PropertiesPropertyResolver(properties);
    }

    private PropertiesPropertyResolver resolverToTest(
            Properties properties,
            Function<String, String> unresolverPropertyHandler
    ) {
        return new PropertiesPropertyResolver(properties, unresolverPropertyHandler);
    }
}
