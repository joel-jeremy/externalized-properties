package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    class ResolveMethodSingleProperty {
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
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should resolve property values from the given properties")
        public void test1() {
            Properties props = new Properties();
            props.setProperty("property.name1", "property.value1");
            props.setProperty("property.name2", "property.value2");
            
            PropertiesPropertyResolver resolver = resolverToTest(props);
            PropertiesPropertyResolver.Result result = resolver.resolve(
                "property.name1",
                "property.name2"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                props.getProperty("property.name1"), 
                result.findRequiredProperty("property.name1")
            );

            assertEquals(
                props.getProperty("property.name2"), 
                result.findRequiredProperty("property.name2")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties " + 
            "when property is not found from the given properties"
        )
        public void test2() {
            PropertiesPropertyResolver resolver = resolverToTest(EMPTY_PROPERTIES);
            PropertiesPropertyResolver.Result result = resolver.resolve(
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

            PropertiesPropertyResolver.Result result = 
                resolver.resolve(
                    "property.unresolvedhandler1", 
                    "property.unresolvedhandler2"
                );
            
            assertTrue(unresolvedPropertyHandlerInvoked.get());
            assertTrue(result.hasResolvedProperties());

            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler1"), 
                result.findRequiredProperty("property.unresolvedhandler1")
            );

            assertEquals(
                unresolvedPropertyHandler.apply("property.unresolvedhandler2"), 
                result.findRequiredProperty("property.unresolvedhandler2")
            );
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
