package io.github.jeyjeyemem.externalizedproperties.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalizedPropertiesExceptionTests {
    @Nested
    class Constructor {
        @Test
        @DisplayName("should set exception message")
        void test1() {
            ExternalizedPropertiesException ex = 
                new ExternalizedPropertiesException("My message");
            assertEquals("My message", ex.getMessage());
        }

        @Test
        @DisplayName("should set exception message and cause")
        void test2() {
            Throwable cause = new RuntimeException();
            ExternalizedPropertiesException ex = 
                new ExternalizedPropertiesException("My message", cause);
            assertEquals("My message", ex.getMessage());
            assertEquals(cause, ex.getCause());
        }
    }
}
