package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    @Test
    @DisplayName("should throw when arg argument is null")
    void test1() {
      assertThrows(
          IllegalArgumentException.class,
          () -> Arguments.requireNonNullOrBlank((String) null, "arg"));
    }

    @Test
    @DisplayName("should throw when arg argument is empty")
    void test2() {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrBlank("", "arg"));
    }

    @Test
    @DisplayName("should throw when arg argument is blank")
    void test3() {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrBlank(" ", "arg"));
    }

    @Test
    @DisplayName("should return non-null arg")
    void test4() {
      String arg = "my-arg";
      String result = Arguments.requireNonNullOrBlank(arg, "arg");

      assertSame(arg, result);
    }
  }

  @Nested
  class RequireNonNullOrEmptyMethodWithStringOverload {
    @Test
    @DisplayName("should throw when arg argument is null")
    void test1() {
      assertThrows(
          IllegalArgumentException.class,
          () -> Arguments.requireNonNullOrEmpty((String) null, "arg"));
    }

    @Test
    @DisplayName("should throw when arg argument is empty")
    void test2() {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrEmpty("", "arg"));
    }

    @Test
    @DisplayName("should return non-null arg")
    void test3() {
      String arg = "my-arg";
      String result = Arguments.requireNonNullOrEmpty(arg, "arg");

      assertSame(arg, result);
    }
  }

  @Nested
  class RequireNonNullOrEmptyMethodWithCollectionOverload {
    @Test
    @DisplayName("should throw when arg collection argument is null")
    void test1() {
      assertThrows(
          IllegalArgumentException.class,
          () -> Arguments.requireNonNullOrEmpty((Collection<?>) null, "arg"));
    }

    @Test
    @DisplayName("should throw when arg collection argument is empty")
    void test2() {
      List<?> arg = Collections.emptyList();
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrEmpty(arg, "arg"));
    }

    @Test
    @DisplayName("should return valid collection")
    void test5() {
      Collection<String> arg = Collections.singleton("my-arg");
      Collection<String> result = Arguments.requireNonNullOrEmpty(arg, "arg");

      assertSame(arg, result);
    }

    @Nested
    class RequireNonNullOrEmptyMethodWithArrayOverload {
      @Test
      @DisplayName("should throw when arg array argument is null")
      void test1() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNonNullOrEmpty((Object[]) null, "arg"));
      }

      @Test
      @DisplayName("should throw when arg array argument is empty")
      void test2() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNonNullOrEmpty(new String[0], "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test5() {
        String[] arg = new String[] {"my-arg"};
        String[] result = Arguments.requireNonNullOrEmpty(arg, "arg");

        assertSame(arg, result);
      }
    }

    @Nested
    class RequireNoNullElementsMethodWithArrayOverload {
      @Test
      @DisplayName("should throw when arg array argument is null")
      void test1() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNoNullElements((Object[]) null, "arg"));
      }

      @Test
      @DisplayName("should throw when arg array argument has null values")
      void test2() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNoNullElements(new String[] {"test", null, "test"}, "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test5() {
        String[] arg = new String[] {"my-arg"};
        String[] result = Arguments.requireNonNullOrEmpty(arg, "arg");

        assertSame(arg, result);
      }
    }

    @Nested
    class RequireNoNullElementsMethodWithCollectionOverload {
      @Test
      @DisplayName("should throw when arg collection argument is null")
      void test1() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNoNullElements((Collection<?>) null, "arg"));
      }

      @Test
      @DisplayName("should throw when arg array argument has null values")
      void test2() {
        List<String> listWithNullValue = Arrays.asList("test", null, "test");
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNoNullElements(listWithNullValue, "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test5() {
        Collection<String> arg = Collections.singleton("my-arg");
        Collection<String> result = Arguments.requireNoNullElements(arg, "arg");

        assertSame(arg, result);
      }
    }
  }
}
