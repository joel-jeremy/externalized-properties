package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ResolverResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemPropertyResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from system properties")
        public void test1() {
            SystemPropertyResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve("java.version");

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getProperty("java.version"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when system property is not found"
        )
        public void test2() {
            SystemPropertyResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "nonexisting.property"
            );
            
            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should resolve values from system properties")
        public void test1() {
            SystemPropertyResolver resolver = resolverToTest();

            String[] propertiesToResolve = new String[] {
                "java.version", 
                "java.home"
            };

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    System.getProperty(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when system property is not found"
        )
        public void test2() {
            SystemPropertyResolver resolver = resolverToTest();

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
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should resolve values from system properties")
        public void test1() {
            SystemPropertyResolver resolver = resolverToTest();

            List<String> propertiesToResolve = Arrays.asList(
                "java.version", 
                "java.home"
            );

            ResolverResult result = resolver.resolve(propertiesToResolve);

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            for (String propertyName : propertiesToResolve) {
                assertEquals(
                    System.getProperty(propertyName), 
                    result.findRequiredProperty(propertyName)
                );
            }
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when system property is not found"
        )
        public void test2() {
            SystemPropertyResolver resolver = resolverToTest();

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
    }

    private SystemPropertyResolver resolverToTest() {
        return new SystemPropertyResolver();
    }
}
