package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
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

  @Nested
  class RequireNonNullOrEmptyMethodWithStringOverload {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should throw when arg argument is null")
    void test1(String arg) {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrEmpty(arg, "arg"));
    }

    @Test
    @DisplayName("should return non-null arg")
    void test2() {
      String arg = "my-arg";
      String result = Arguments.requireNonNullOrEmpty(arg, "arg");

      assertSame(arg, result);
    }
  }

  @Nested
  class RequireNonNullOrEmptyMethodWithCollectionOverload {
    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("should throw when arg collection argument is null")
    void test1(List<?> arg) {
      assertThrows(
          IllegalArgumentException.class, () -> Arguments.requireNonNullOrEmpty(arg, "arg"));
    }

    @Test
    @DisplayName("should return valid collection")
    void test2() {
      Collection<String> arg = Collections.singleton("my-arg");
      Collection<String> result = Arguments.requireNonNullOrEmpty(arg, "arg");

      assertSame(arg, result);
    }

    @Nested
    class RequireNonNullOrEmptyMethodWithArrayOverload {
      @ParameterizedTest
      @NullAndEmptySource
      @DisplayName("should throw when arg array argument is null")
      void test1(Object[] arg) {
        assertThrows(
            IllegalArgumentException.class, () -> Arguments.requireNonNullOrEmpty(arg, "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test2() {
        String[] arg = new String[] {"my-arg"};
        String[] result = Arguments.requireNonNullOrEmpty(arg, "arg");

        assertSame(arg, result);
      }
    }

    @Nested
    class RequireNoNullElementsMethodWithArrayOverload {
      @ParameterizedTest
      @NullSource
      @ValueSource(strings = "test1,null,test3")
      @DisplayName("should throw when arg array argument is null")
      void test1(@ConvertWith(JUnitArrayConverter.class) Object[] arg) {
        assertThrows(
            IllegalArgumentException.class,
            () -> Arguments.requireNoNullElements((Object[]) null, "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test2() {
        String[] arg = new String[] {"my-arg"};
        String[] result = Arguments.requireNonNullOrEmpty(arg, "arg");

        assertSame(arg, result);
      }
    }

    @Nested
    class RequireNoNullElementsMethodWithCollectionOverload {
      @ParameterizedTest
      @NullSource
      @ValueSource(strings = "test1,null,test3")
      @DisplayName("should throw when arg collection argument is null")
      void test1(@ConvertWith(JUnitCollectionConverter.class) Collection<String> arg) {
        assertThrows(
            IllegalArgumentException.class, () -> Arguments.requireNoNullElements(arg, "arg"));
      }

      @Test
      @DisplayName("should return valid array")
      void test2() {
        Collection<String> arg = Collections.singleton("my-arg");
        Collection<String> result = Arguments.requireNoNullElements(arg, "arg");

        assertSame(arg, result);
      }
    }
  }

  static class JUnitArrayConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context)
        throws ArgumentConversionException {
      if (source instanceof String) {
        String s = (String) source;
        // Replaces literal "null" with null.
        return Stream.of(s.split(",")).map(e -> "null".equals(e) ? null : e).toArray();
      }
      return source;
    }
  }

  static class JUnitCollectionConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context)
        throws ArgumentConversionException {
      if (source instanceof String) {
        String s = (String) source;
        // Replaces literal "null" with null.
        return Stream.of(s.split(","))
            .map(e -> "null".equals(e) ? null : e)
            .collect(Collectors.toList());
      }
      return source;
    }
  }
}
