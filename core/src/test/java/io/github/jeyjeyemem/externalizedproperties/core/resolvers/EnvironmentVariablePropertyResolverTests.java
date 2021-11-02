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
            ExternalizedPropertyResolverResult result = resolver.resolve("PATH");

            System.out.println(System.getenv().keySet());

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getenv("PATH"), 
                result.findResolvedProperty("PATH")
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
            ExternalizedPropertyResolverResult result = resolver.resolve("NON_EXISTING_ENVVAR");
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("NON_EXISTING_ENVVAR"));
        }
    }

    private EnvironmentVariablePropertyResolver resolverToTest() {
        return new EnvironmentVariablePropertyResolver();
    }
}
