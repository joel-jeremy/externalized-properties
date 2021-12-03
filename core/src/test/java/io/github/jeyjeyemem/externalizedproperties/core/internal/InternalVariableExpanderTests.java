package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.VariableExpansionException;
import io.github.jeyjeyemem.externalizedproperties.core.resolvers.SystemPropertyResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InternalVariableExpanderTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should throw when resolvers argument is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalVariableExpander(null)
            );
        }

        @Test
        @DisplayName("should throw when variable prefix argument is null or empty")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalVariableExpander(
                    new SystemPropertyResolver(),
                    null,
                    "}"
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalVariableExpander(
                    new SystemPropertyResolver(),
                    "",
                    "}"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable suffix argument is null or empty")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalVariableExpander(
                    new SystemPropertyResolver(),
                    "${",
                    null
                )
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> new InternalVariableExpander(
                    new SystemPropertyResolver(),
                    "${",
                    ""
                )
            );
        }
    }

    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should throw when value argument is null")
        public void validationTest1() {
            InternalVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            assertThrows(
                IllegalArgumentException.class, 
                () -> variableExpander.expandVariables(null)
            );
        }

        @Test
        @DisplayName("should expand variable with value from resolver")
        public void test1() {
            SystemPropertyResolver resolver = new SystemPropertyResolver();
            InternalVariableExpander variableExpander = variableExpander(
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
        @DisplayName("should expand multiple variables with values from resolvers")
        public void test2() {
            SystemPropertyResolver resolver = new SystemPropertyResolver();
            InternalVariableExpander variableExpander = variableExpander(
                resolver
            );

            String result = variableExpander.expandVariables(
                "property-${java.version}-home-${java.home}"
            );

            String javaVersionProeprty = resolver.resolve("java.version").orElse(null);
            String javaHomeProperty = resolver.resolve("java.home").orElse(null);

            assertEquals(
                "property-" + javaVersionProeprty + "-home-" + javaHomeProperty, 
                result
            );
        }

        @Test
        @DisplayName("should return original string when there are no variables")
        public void test3() {
            InternalVariableExpander variableExpander =  variableExpander(
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
        public void test4() {
            InternalVariableExpander variableExpander = variableExpander(
                new SystemPropertyResolver()
            );

            assertThrows(
                VariableExpansionException.class, 
                () -> variableExpander.expandVariables("property-${nonexistent}")
            );
        }

        @Test
        @DisplayName("should return empty when value is empty")
        public void test5() {
            InternalVariableExpander variableExpander = variableExpander(
                new SystemPropertyResolver()
            );

            assertEquals("", variableExpander.expandVariables(""));
        }

        @Test
        @DisplayName(
            "should skip expansion when there is no variable name between " +
            "variable prefix and variable suffix"
        )
        public void test6() {
            InternalVariableExpander variableExpander =  variableExpander(
                new SystemPropertyResolver()
            );

            String result = variableExpander.expandVariables("test-${}");

            assertEquals("test-${}", result);
        }

        @Test
        @DisplayName(
            "should skip expansion " + 
            "when there is there is a variable prefix detected but no variable suffix"
        )
        public void test7() {
            InternalVariableExpander variableExpander = variableExpander(
                new SystemPropertyResolver()
            );

            String result = variableExpander.expandVariables("test-${variable");

            assertEquals("test-${variable", result);
        }
        
        @Test
        @DisplayName(
            "should expand variable with value from resolver using custom prefix and suffix"
        )
        public void test8() {
            SystemPropertyResolver resolver = new SystemPropertyResolver();
            InternalVariableExpander variableExpander = variableExpander(
                resolver,
                "#",
                "^"
            );

            String result = variableExpander.expandVariables("property-#java.version^");

            String propertyValue = resolver.resolve("java.version").orElse(null);

            assertEquals(
                "property-" + propertyValue, 
                result
            );
        }
    }

    private InternalVariableExpander variableExpander(
            ExternalizedPropertyResolver externalizedPropertyResolver
    ) {
        return new InternalVariableExpander(externalizedPropertyResolver);
    }

    private InternalVariableExpander variableExpander(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            String variablePrefix,
            String variableSuffix
    ) {
        return new InternalVariableExpander(
            externalizedPropertyResolver,
            variablePrefix,
            variableSuffix
        );
    }
}
