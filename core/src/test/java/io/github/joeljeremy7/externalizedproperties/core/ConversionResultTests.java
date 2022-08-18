package io.github.joeljeremy7.externalizedproperties.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConversionResultTests {
    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when value argument is null")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ConversionResult.of(null)
            );
        }
    }
    
    @Nested
    class SkipMethod {
        @Test
        @DisplayName("should always return the same object reference")
        void test1() {
            assertSame(ConversionResult.skip(), ConversionResult.skip());
        }
    }

    @Nested
    class ValueMethod {
        @Test
        @DisplayName("should return the provided value via of() factory method")
        void test1() {
            ConversionResult<String> result = ConversionResult.of("test");
            assertEquals("test", result.value());
        }

        @Test
        @DisplayName(
            "should throw when result object is a skip result " + 
            "created via skip() factory method."
        )
        void test2() {
            ConversionResult<String> skipResult = ConversionResult.skip();
            assertThrows(
                IllegalStateException.class, 
                () -> skipResult.value()
            );
        }
    }

    @Nested
    class HashCodeMethod {
        @Test
        @DisplayName("should return hash of the value")
        void test1() {
            int value = 1;
            ConversionResult<?> result = ConversionResult.of(value);
            assertEquals(Objects.hashCode(value), result.hashCode());
        }
    }

    @Nested
    class EqualsMethod {
        @Test
        @DisplayName(
            "should return object is the same reference"
        )
        void test1() {
            ConversionResult<?> result1 = ConversionResult.of(1);
            ConversionResult<?> result2 = result1;
            assertTrue(result1.equals(result2));
        }

        @Test
        @DisplayName(
            "should return true both result's values are equal"
        )
        void test2() {
            ConversionResult<?> result1 = ConversionResult.of(1);
            ConversionResult<?> result2 = ConversionResult.of(1);
            assertTrue(result1.equals(result2));
        }

        @Test
        @DisplayName(
            "should return false when object is null"
        )
        void test3() {
            ConversionResult<?> result1 = ConversionResult.of(1);
            assertFalse(result1.equals(null));
        }

        @Test
        @DisplayName(
            "should return false when object is not a ConversionResult instance"
        )
        void test4() {
            ConversionResult<?> result1 = ConversionResult.of(1);
            assertFalse(result1.equals(new Object()));
        }
    }
}