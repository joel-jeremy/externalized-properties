package io.github.jeyjeyemem.externalizedproperties.core.conversion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConversionExceptionTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should set exception message")
        void test1() {
            ConversionException ex = 
                new ConversionException("My message");
            assertEquals("My message", ex.getMessage());
        }

        @Test
        @DisplayName("should set exception message and cause")
        void test2() {
            Throwable cause = new RuntimeException();
            ConversionException ex = 
                new ConversionException("My message", cause);
            assertEquals("My message", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }
    }
}
