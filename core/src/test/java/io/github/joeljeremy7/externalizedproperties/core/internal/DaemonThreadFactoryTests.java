package io.github.joeljeremy7.externalizedproperties.core.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DaemonThreadFactoryTests {
    @Nested
    class NewThreadMethod {
        @Test
        @DisplayName("should return a daemon thread")
        void test1() {
            DaemonThreadFactory daemonThreadFactory = 
                new DaemonThreadFactory("test");
            Thread thread = daemonThreadFactory.newThread(() -> {});

            assertTrue(thread.isDaemon());
        }

        @Test
        @DisplayName("should return a daemon thread with the specified prefix")
        void test2() {
            DaemonThreadFactory daemonThreadFactory = 
                new DaemonThreadFactory("test");
            Thread thread = daemonThreadFactory.newThread(() -> {});

            assertTrue(thread.getName().startsWith("test"));
        }
    }
}
