package io.github.jeyjeyemem.externalizedproperties.core.resolvers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolverResult;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SystemPropertyResolverTests {
    @Nested
    class ResolveMethod {
        @Test
        @DisplayName("should resolve values from system properties")
        public void test1() {
            SystemPropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve("java.version");

            assertTrue(result.hasResolvedProperties());
            assertFalse(result.hasUnresolvedProperties());

            assertEquals(
                System.getProperty("java.version"), 
                result.findResolvedProperty("java.version")
                    .map(ResolvedProperty::value)
                    .orElse(null)
            );
        }

        @Test
        @DisplayName(
            "should return result with unresolved properties when system property is not found"
        )
        public void test2() {
            SystemPropertyResolver resolver = resolverToTest();
            ExternalizedPropertyResolverResult result = resolver.resolve("nonexisting.property");
            
            assertTrue(result.hasUnresolvedProperties());
            assertTrue(result.unresolvedPropertyNames().contains("nonexisting.property"));
        }
    }

    private SystemPropertyResolver resolverToTest() {
        return new SystemPropertyResolver();
    }
}
