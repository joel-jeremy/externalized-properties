package io.github.joeljeremy7.externalizedproperties.core.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentsTests {
    @Nested
    class RequireNonNullMethod {
        @Test
        @DisplayName("should throw when arg argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNull(null, "arg")
            );
        }

        @Test
        @DisplayName("should return non-null arg.")
        void test4() {
            String arg = "my-arg";
            String result = Arguments.requireNonNull(arg, "arg.");

            assertSame(arg, result);
        }
    }

    @Nested
    class RequireNonNullOrEmptyStringMethod {
        @Test
        @DisplayName("should throw when arg argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNullOrEmptyString(
                    null,
                    "arg must not be null."
                )
            );
        }

        @Test
        @DisplayName("should return non-null arg.")
        void test4() {
            String arg = "my-arg";
            String result = Arguments.requireNonNullOrEmptyString(
                arg,
                "arg"
            );

            assertSame(arg, result);
        }
    }

    @Nested
    class RequireNonNullOrEmptyCollectionMethod {
        @Test
        @DisplayName("should throw when arg collection argument is null.")
        void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNullOrEmptyCollection(
                    null,
                    "arg"
                )
            );
        }
        
        @Test
        @DisplayName("should throw when arg collection argument is empty.")
        void test2() {
            List<?> arg = Collections.emptyList();
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNullOrEmptyCollection(
                    arg,
                    "arg"
                )
            );
        }

        @Test
        @DisplayName("should return non-null or empty collection arg.")
        void test5() {
            Collection<String> arg = Collections.singleton("my-arg");
            Collection<String> result = Arguments.requireNonNullOrEmptyCollection(
                arg,
                "arg"
            );

            assertSame(arg, result);
        }

        @Nested
        class RequireNonNullOrEmptyArrayMethod {
            @Test
            @DisplayName("should throw when arg array argument is null.")
            void test1() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.requireNonNullOrEmptyArray(
                        null,
                        "arg"
                    )
                );
            }
            
            @Test
            @DisplayName("should throw when arg array argument is empty.")
            void test2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.requireNonNullOrEmptyArray(
                        new String[0],
                        "arg"
                    )
                );
            }

            @Test
            @DisplayName("should return non-null or empty collection arg.")
            void test5() {
                String[] arg = new String[] { "my-arg" };
                String[] result = Arguments.requireNonNullOrEmptyArray(
                    arg,
                    "arg"
                );

                assertSame(arg, result);
            }
        }
    }
}
