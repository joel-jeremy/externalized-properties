package io.github.jeyjeyemem.externalizedproperties.core.internal.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArgumentsTests {
    @Nested
    class RequireNonNullMethod {
        @Test
        @DisplayName("should throw when arg argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNull(null, "arg")
            );
        }

        @Test
        @DisplayName("should throw when arg name argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNull("my-arg", null)
            );
        }

        @Test
        @DisplayName("should throw when arg name argument is empty.")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.requireNonNull("my-arg", "")
            );
        }


        @Test
        @DisplayName("should return non-null arg.")
        public void test4() {
            String arg = "my-arg";
            String result = Arguments.requireNonNull(arg, "arg must not be null.");

            assertSame(arg, result);
        }
    }

    @Nested
    class RequireMethod {
        @Test
        @DisplayName("should throw when arg argument is null.")
        public void test1() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.require(null, Objects::nonNull, "arg must not be null.")
            );
        }

        @Test
        @DisplayName("should throw when requirement argument is null.")
        public void test2() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.require("my-arg", null, "requirement must not be null.")
            );
        }

        @Test
        @DisplayName("should throw when exception message is null.")
        public void test3() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.require("my-arg", Objects::nonNull, null)
            );
        }

        @Test
        @DisplayName("should return arg when requirement is met.")
        public void test4() {
            String arg = "my-arg";
            String result = Arguments.require(arg, Objects::nonNull, "arg must not be null.");

            assertSame(arg, result);
        }

        @Test
        @DisplayName("should throw when requirement is not met.")
        public void test5() {
            assertThrows(
                IllegalArgumentException.class, 
                () -> Arguments.require(1, a -> a != 1, "arg must not be 1.")
            );
        }
    }

    @Nested
    class ArgumentsStrings {
        @Nested
        class RequireNonNullOrEmptyStringMethod {
            @Test
            @DisplayName("should throw when arg argument is null.")
            public void test1() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Strings.requireNonNullOrEmptyString(
                        null,
                        "arg must not be null."
                    )
                );
            }

            @Test
            @DisplayName("should throw when arg name argument is null.")
            public void test2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Strings.requireNonNullOrEmptyString(
                        "my-arg",
                        null
                    )
                );
            }

            @Test
            @DisplayName("should throw when arg name argument is empty.")
            public void test3() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Strings.requireNonNullOrEmptyString(
                        "my-arg",
                        ""
                    )
                );
            }

            @Test
            @DisplayName("should return non-null arg.")
            public void test4() {
                String arg = "my-arg";
                String result = Arguments.Strings.requireNonNullOrEmptyString(
                    arg,
                    "arg"
                );

                assertSame(arg, result);
            }
        }
    }

    @Nested
    class ArgumentsCollections {
        @Nested
        class RequireNonNullOrEmptyCollectionMethod {
            @Test
            @DisplayName("should throw when arg collection argument is null.")
            public void test1() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Collections.requireNonNullOrEmptyCollection(
                        null,
                        "arg"
                    )
                );
            }
            
            @Test
            @DisplayName("should throw when arg collection argument is empty.")
            public void test2() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Collections.requireNonNullOrEmptyCollection(
                        Collections.emptyList(),
                        "arg"
                    )
                );
            }

            @Test
            @DisplayName("should throw when arg name argument is null.")
            public void test3() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Collections.requireNonNullOrEmptyCollection(
                        Collections.singleton("1"),
                        null
                    )
                );
            }

            @Test
            @DisplayName("should throw when arg name argument is empty.")
            public void test4() {
                assertThrows(
                    IllegalArgumentException.class, 
                    () -> Arguments.Collections.requireNonNullOrEmptyCollection(
                        Collections.singleton("1"),
                        ""
                    )
                );
            }

            @Test
            @DisplayName("should return non-null or empty collection arg.")
            public void test5() {
                Collection<String> arg = Collections.singleton("my-arg");
                Collection<String> result = Arguments.Collections.requireNonNullOrEmptyCollection(
                    arg,
                    "arg"
                );

                assertSame(arg, result);
            }
        }
    }
}
