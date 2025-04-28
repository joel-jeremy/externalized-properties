package io.github.joeljeremy.externalizedproperties.git.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class ArgumentsTests {
  @Nested
  class RequireNonNullMethod {
    @Test
    @DisplayName("should throw when arg argument is null")
    void test1() {
      assertThrows(IllegalArgumentException.class, () -> Arguments.requireNonNull(null, "arg"));
    }

    @Test
    @DisplayName("should return non-null arg")
    void test4() {
      String arg = "my-arg";
      String result = Arguments.requireNonNull(arg, "arg");

      assertSame(arg, result);
    }
  }

  @Nested
  class RequireNonNullOrBlank {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName("should throw when arg argument is null")
    void test1(String arg) {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrBlank(arg, "arg"));
    }

    @Test
    @DisplayName("should return non-null arg")
    void test2() {
      String arg = "my-arg";
      String result = Arguments.requireNonNullOrBlank(arg, "arg");

      assertSame(arg, result);
    }
  }
}
