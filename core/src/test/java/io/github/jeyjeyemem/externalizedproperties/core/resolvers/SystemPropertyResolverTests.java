package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemPropertyResolverTests {
    @Nested
    class ResolveMethodSingleProperty {
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
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should resolve values from system properties")
        public void test1() {
            SystemPropertyResolver resolver = resolverToTest();
            SystemPropertyResolver.Result result = resolver.resolve("java.version", "java.home");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findRequiredProperty("java.version")
            );

            assertEquals(
                System.getProperty("java.home"), 
                result.findRequiredProperty("java.home")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when system property is not found"
        )
        public void test2() {
            SystemPropertyResolver resolver = resolverToTest();
            SystemPropertyResolver.Result result = resolver.resolve(
                "nonexisting.property1", 
                "nonexisting.property2"
            );
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property1"));
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property2"));
        }
    }

    private SystemPropertyResolver resolverToTest() {
        return new SystemPropertyResolver();
    }
}
