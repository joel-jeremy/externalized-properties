package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.ResolvedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.StringVariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InternalStringVariableExpanderTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolvers argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalStringVariableExpander(null)
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should expand variables in the given string")
        public void validationTest1() {
            InternalStringVariableExpander variableExpander =  variableExpander(
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
            InternalStringVariableExpander variableExpander = variableExpander(
                resolver
            );

            String result = variableExpander.expandVariables("property-${java.version}");

            String resolverValue = resolver.resolve("java.version")
                .findResolvedProperty("java.version")
                .map(ResolvedProperty::value)
                .orElse(null);

            assertEquals(
                "property-" + resolverValue, 
                result
            );
        }

        @Test
        @DisplayName("should return same string when there are no variables")
        public void test2() {
            InternalStringVariableExpander variableExpander =  variableExpander(
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
            InternalStringVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertThrows(
                StringVariableExpansionException.class, 
                () -> variableExpander.expandVariables("property-${nonexistent}")
            );
        }

        @Test
        @DisplayName("should return empty when value is empty")
        public void test4() {
            InternalStringVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertEquals("", variableExpander.expandVariables(""));
        }
    }

    private InternalStringVariableExpander variableExpander(
            ExternalizedPropertyResolver externalizedPropertyResolver
    ) {
        return new InternalStringVariableExpander(externalizedPropertyResolver);
    }
}
