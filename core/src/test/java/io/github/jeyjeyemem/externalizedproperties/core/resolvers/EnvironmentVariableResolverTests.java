package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentVariableResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve property value from environment variables")
        public void test1() {
            EnvironmentVariableResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "PATH"
            );

            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(
                System.getenv("PATH"), 
                result.get()
            );
        }

        @Test
        @DisplayName(
            "should return empty Optional when environment variable is not found"
        )
        public void test2() {
            EnvironmentVariableResolver resolver = resolverToTest();
            Optional<String> result = resolver.resolve(
                "NON_EXISTING_ENVVAR"
            );

            assertNotNull(result);
            assertFalse(result.isPresent());
        }
    }

    @Nested
    class ResolveMethodWithVarArgsOverload {
        @Test
        @DisplayName("should resolve property values from environment variables")
        public void test1() {
            EnvironmentVariableResolver resolver = resolverToTest();
            EnvironmentVariableResolver.Result result = resolver.resolve(
                "PATH",
                "HOME"
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredProperty("PATH")
            );

            assertEquals(
                System.getenv("HOME"), 
                result.findRequiredProperty("HOME")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when environment variable is not found"
        )
        public void test2() {
            EnvironmentVariableResolver resolver = resolverToTest();
            EnvironmentVariableResolver.Result result = resolver.resolve(
                "NON_EXISTING_ENVVAR1",
                "NON_EXISTING_ENVVAR2"
            );
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR1"));
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR2"));
        }
    }

    @Nested
    class ResolveMethodWithCollectionOverload {
        @Test
        @DisplayName("should resolve property values from environment variables")
        public void test1() {
            EnvironmentVariableResolver resolver = resolverToTest();
            EnvironmentVariableResolver.Result result = resolver.resolve(
                Arrays.asList(
                    "PATH",
                    "HOME"
                )
            );

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("PATH"), 
                result.findRequiredProperty("PATH")
            );

            assertEquals(
                System.getenv("HOME"), 
                result.findRequiredProperty("HOME")
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when environment variable is not found"
        )
        public void test2() {
            EnvironmentVariableResolver resolver = resolverToTest();
            EnvironmentVariableResolver.Result result = resolver.resolve(
                Arrays.asList(
                    "NON_EXISTING_ENVVAR1",
                    "NON_EXISTING_ENVVAR2"
                )
            );
            
            assertFalse(result.hasResolvedProperties());
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR1"));
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR2"));
        }
    }

    private EnvironmentVariableResolver resolverToTest() {
        return new EnvironmentVariableResolver();
    }
}
