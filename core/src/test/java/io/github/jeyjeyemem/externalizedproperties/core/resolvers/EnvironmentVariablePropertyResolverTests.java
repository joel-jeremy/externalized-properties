package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvironmentVariablePropertyResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve values from environment variables")
        public void test1() {
            EnvironmentVariablePropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve("JAVA_HOME");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("JAVA_HOME"), 
                result.findResolvedProperty("JAVA_HOME")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when environment variable is not found"
        )
        public void test2() {
            EnvironmentVariablePropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.envvar");
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.envvar"));
        }
    }

    private EnvironmentVariablePropertyResolver resolverToTest() {
        return new EnvironmentVariablePropertyResolver();
    }
}
