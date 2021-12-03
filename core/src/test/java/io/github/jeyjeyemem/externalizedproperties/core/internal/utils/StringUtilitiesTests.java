package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringUtilitiesTests {
    @Nested
    class ReplaceVariablesMethod {
        @Test
        @DisplayName("should throw when value argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> StringUtilities.replaceVariables(null, v -> "variable")  
            );
        }

        @Test
        @DisplayName("should throw when variable value provider argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> StringUtilities.replaceVariables("value", null)  
            );
        }

        @Test
        @DisplayName("should replace variables that matches defautlt pattern i.e. ${variable}.")
        public void test3() {
            Function<String, String> variableValueProvider = name -> name + "-variable-value";

            String result = 
                StringUtilities.replaceVariables("${my-variable}-test", variableValueProvider);

            assertEquals(
                variableValueProvider.apply("my-variable") + "-test",
                result
            );
        }

        @Test
        @DisplayName("should throw when no replacement value can be found for a variable.")
        public void test4() {
            Function<String, String> noValueProvider = name -> null;

            assertThrows(
                IllegalStateException.class, 
                () -> StringUtilities.replaceVariables("${my-variable}-test", noValueProvider)
            );
        }

        @Test
        @DisplayName("should propagate exception when variable value provider throws an exception.")
        public void test5() {
            Function<String, String> throwingProvider = 
                name -> { 
                    throw new ExternalizedPropertiesException("Mr. Stark, I don't feel so good...");
                };

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> StringUtilities.replaceVariables("${my-variable}-test", throwingProvider)
            );
        }

        @Test
        @DisplayName("should not expand when matched placeholder does not contain a variable name.")
        public void test6() {
            Function<String, String> variableValueProvider = name -> name + "-variable-value";

            assertEquals(
                "${}-test",
                StringUtilities.replaceVariables("${}-test", variableValueProvider)
            );
        }
    }

    @Nested
    class ReplaceVariablesMethodWithVariablePattern {
        @Test
        @DisplayName("should throw when value argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> StringUtilities.replaceVariables(
                    null, 
                    StringUtilities.DEFAULT_VARIABLE_PATTERN, 
                    v -> "variable"
                )
            );
        }

        @Test
        @DisplayName("should throw when variable pattern argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> StringUtilities.replaceVariables("value", null, v -> "variable")  
            );
        }

        @Test
        @DisplayName("should throw when variable value provider argument is null.")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> StringUtilities.replaceVariables(
                    "value", 
                    StringUtilities.DEFAULT_VARIABLE_PATTERN, 
                    null
                )  
            );
        }

        @Test
        @DisplayName("should replace variables that matches given pattern.")
        public void test4() {
            Function<String, String> variableValueProvider = name -> name + "-variable-value";

            // Custom pattern matches #(variable)
            Pattern pattern = Pattern.compile("\\#\\((.+?)\\)");

            String result = 
                StringUtilities.replaceVariables(
                    "#(my-variable)-test", 
                    pattern,
                    variableValueProvider
                );

            assertEquals(
                variableValueProvider.apply("my-variable") + "-test",
                result
            );
        }

        @Test
        @DisplayName("should throw when no replacement value can be found for a variable.")
        public void test5() {
            Function<String, String> noValueProvider = name -> null;

            // Custom pattern matches #(variable)
            Pattern pattern = Pattern.compile("\\#\\((.+?)\\)");

            assertThrows(
                IllegalStateException.class, 
                () -> StringUtilities.replaceVariables(
                    "#(my-variable)-test", 
                    pattern,
                    noValueProvider
                )
            );
        }

        @Test
        @DisplayName("should propagate exception when variable value provider throws an exception.")
        public void test6() {
            Function<String, String> throwingProvider = 
                name -> { 
                    throw new ExternalizedPropertiesException("Mr. Stark, I don't feel so good...");
                };
            
            // Custom pattern matches #(variable)
            Pattern pattern = Pattern.compile("\\#\\((.+?)\\)");

            assertThrows(
                ExternalizedPropertiesException.class, 
                () -> StringUtilities.replaceVariables(
                    "#(my-variable)-test", 
                    pattern, 
                    throwingProvider
                )
            );
        }
    }
}
