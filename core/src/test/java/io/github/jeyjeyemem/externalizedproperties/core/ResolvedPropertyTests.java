package io.github.jeyjeyemem.externalizedproperties.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResolvedPropertyTests {
    @Nested
    class WithMethod {
        @Test
        @DisplayName("should throw when name is null")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolvedProperty.with(null, "value")
            );
        }

        @Test
        @DisplayName("should throw when name is empty")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolvedProperty.with("", "value")
            );
        }

        @Test
        @DisplayName("should throw when value is null")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ResolvedProperty.with("name", null)
            );
        }

        @Test
        @DisplayName("should allow empty value")
        public void test4() {
            ResolvedProperty resolvedProperty = ResolvedProperty.with("name", "");

            assertEquals("name", resolvedProperty.name());
            assertEquals("", resolvedProperty.value());
        }
    }

    @Nested
    class WithValueMethod {
        @Test
        @DisplayName("should throw when value is null")
        public void test1() {
            ResolvedProperty resolvedProperty = ResolvedProperty.with("name", "value");
            assertThrows(
                IllegalArgumentException.class, 
                () -> resolvedProperty.withValue(null)
            );
        }

        @Test
        @DisplayName("should reuse property name")
        public void test2() {
            ResolvedProperty resolvedProperty = ResolvedProperty.with("name", "value");
            ResolvedProperty updated = resolvedProperty.withValue("new-value");

            assertEquals(resolvedProperty.name(), updated.name());
            assertEquals("new-value", updated.value());
        }

        @Test
        @DisplayName("should allow empty value")
        public void test3() {
            ResolvedProperty resolvedProperty = ResolvedProperty.with("name", "value");
            ResolvedProperty updated = resolvedProperty.withValue("");

            assertEquals(resolvedProperty.name(), updated.name());
            assertEquals("", updated.value());
        }
    }
}
