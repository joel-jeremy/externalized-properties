package io.github.joeljeremy7.externalizedproperties.resolvers.database;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DatabasePropertyTests {
    @Nested
    class WithMethod {
        @Test
        @DisplayName("should throw when name is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseProperty.with(null, "value")
            );
        }

        @Test
        @DisplayName("should throw when name is empty")
        void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseProperty.with("", "value")
            );
        }

        @Test
        @DisplayName("should throw when value is null")
        void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> DatabaseProperty.with("name", null)
            );
        }

        @Test
        @DisplayName("should allow empty value")
        void test4() {
            DatabaseProperty resolvedProperty = DatabaseProperty.with("name", "");

            assertEquals("name", resolvedProperty.name());
            assertEquals("", resolvedProperty.value());
        }
    }
}
