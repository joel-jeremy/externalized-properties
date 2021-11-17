package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentPropertyResolverTests {
    @Nested
    class ResolveMethodSingleProperty {
        @Test
        @DisplayName("should resolve property value from environment variables")
        public void test1() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            Optional<ResolvedProperty> result = resolver.resolve(
                "PATH"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getenv("PATH"), 
                result.get().value()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when environment variable is not found"
        )
        public void test2() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            Optional<ResolvedProperty> result = resolver.resolve(
                "NON_EXISTING_ENVVAR"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class ResolveMethodMultipleProperties {
        @Test
        @DisplayName("should resolve property values from environment variables")
        public void test1() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve(
                "PATH",
                "HOME"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredPropertyValue("PATH")
            );

            assertEquals(
                System.getenv("HOME"), 
                result.findRequiredPropertyValue("HOME")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when environment variable is not found"
        )
        public void test2() {
            EnvironmentPropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve(
                "NON_EXISTING_ENVVAR1",
                "NON_EXISTING_ENVVAR2"
            );
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR1"));
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR2"));
        }
    }

    private EnvironmentPropertyResolver resolverToTest() {
        return new EnvironmentPropertyResolver();
    }
}
