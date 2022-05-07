package io.github.joeljeremy7.externalizedproperties.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConversionResultTests {
    @Nested
    class OfMethod {
        @Test
        @DisplayName("should throw when value argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> ConversionResult.of(null)
            );
        }
    }
    
    @Nested
    class SkipMethod {
        @Test
        @DisplayName("should always return the same object reference.")
        public void test1() {
            assertSame(ConversionResult.skip(), ConversionResult.skip());
        }
    }

    @Nested
    class ValueMethod {
        @Test
        @DisplayName("should return the provided value via of() factory method.")
        public void test1() {
            ConversionResult<String> result = ConversionResult.of("test");
            assertEquals("test", result.value());
        }

        @Test
        @DisplayName(
            "should throw when result object is a skip result " + 
            "created via skip() factory method."
        )
        public void test2() {
            ConversionResult<String> skipResult = ConversionResult.skip();
            assertThrows(
                IllegalStateException.class, 
                () -> skipResult.value()
            );
        }
    }
}