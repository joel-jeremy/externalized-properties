package io.github.jeyjeyemem.externalizedproperties.core.variableexpansion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class NoOpVariableExpanderTests {
    @Nested
    class ExpandVariablesMethod {
        @Test
        @DisplayName("should just return the input value")
        public void test1() {
            String value = "${test}";
            String result = NoOpVariableExpander.INSTANCE.expandVariables(value);

            assertSame(value, result);
        }
    }
}
