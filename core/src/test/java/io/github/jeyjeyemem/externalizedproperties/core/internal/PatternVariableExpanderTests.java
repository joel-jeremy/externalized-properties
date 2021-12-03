package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PatternVariableExpanderTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolvers argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new PatternVariableExpander(null)
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should throw when value argument is null")
        public void validationTest1() {
            PatternVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> variableExpander.expandVariables(null)
            );
        }

        @Test
        @DisplayName("should expand variables with values from resolvers")
        public void test1() {
            SystemPropertyResolver resolver = new SystemPropertyResolver();
            PatternVariableExpander variableExpander = variableExpander(
                resolver
            );

            String result = variableExpander.expandVariables("property-${java.version}");

            String propertyValue = resolver.resolve("java.version").orElse(null);

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }

        @Test
        @DisplayName("should return same string when there are no variables")
        public void test2() {
            PatternVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            String result = variableExpander.expandVariables("property-no-variables");

            assertEquals(
                "property-no-variables", 
                result
            );
        }

        @Test
        @DisplayName("should throw when variable cannot be resolver from any resolvers")
        public void test3() {
            PatternVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertThrows(
                VariableExpansionException.class, 
                () -> variableExpander.expandVariables("property-${nonexistent}")
            );
        }

        @Test
        @DisplayName("should return empty when value is empty")
        public void test4() {
            PatternVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertEquals("", variableExpander.expandVariables(""));
        }
    }

    private PatternVariableExpander variableExpander(
            ExternalizedPropertyResolver externalizedPropertyResolver
    ) {
        return new PatternVariableExpander(externalizedPropertyResolver);
    }
}
