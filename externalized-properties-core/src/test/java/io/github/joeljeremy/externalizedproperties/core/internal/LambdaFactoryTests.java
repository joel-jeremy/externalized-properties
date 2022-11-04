package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.awt.List;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LambdaFactoryTests {
  static final Method DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::defaultMethod);

  static final Method NON_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          NonDefaultMethodInterface.class, NonDefaultMethodInterface::nonDefaultMethod);

  @Nested
  class CreateLambdaFunctionMethod {
    @Test
    @DisplayName("should throw when functional interface argument is not a functional interface")
    void test1() {
      assertThrows(
          IllegalArgumentException.class,
          () -> LambdaFactory.createLambdaFunction(DEFAULT_INTERFACE_METHOD, List.class));
    }

    @Test
    @DisplayName(
        "should create a lambda which invokes the target method (default interface method)")
    void test2() throws Throwable {
      @SuppressWarnings("unchecked")
      BiFunction<Object, Object, Object> lambda =
          LambdaFactory.createLambdaFunction(DEFAULT_INTERFACE_METHOD, BiFunction.class);

      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";
      String expected = instance.defaultMethod(value);
      Object result = lambda.apply(instance, value);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName("should create a lambda which invokes the target method")
    void test3() throws Throwable {
      @SuppressWarnings("unchecked")
      BiFunction<Object, Object, Object> lambda =
          LambdaFactory.createLambdaFunction(NON_DEFAULT_INTERFACE_METHOD, BiFunction.class);

      NonDefaultMethodInterface instance =
          new NonDefaultMethodInterface() {
            @Override
            public String nonDefaultMethod(String defaultValue) {
              return defaultValue;
            }
          };

      String value = "test";
      String expected = instance.nonDefaultMethod(value);
      Object actual = lambda.apply(instance, value);

      assertEquals(expected, actual);
    }
  }

  @Nested
  class CreateLambdaFunctionMethodWithSpecialCallerOverload {
    @Test
    @DisplayName("should throw when target method argument is not a default interface method")
    void test1() {
      assertThrows(
          AbstractMethodError.class,
          () ->
              LambdaFactory.createLambdaFunction(
                  NON_DEFAULT_INTERFACE_METHOD,
                  BiFunction.class,
                  NON_DEFAULT_INTERFACE_METHOD.getDeclaringClass()));
    }

    @Test
    @DisplayName("should throw when functional interface argument is not a functional interface")
    void test2() {
      assertThrows(
          IllegalArgumentException.class,
          () ->
              LambdaFactory.createLambdaFunction(
                  DEFAULT_INTERFACE_METHOD,
                  List.class,
                  DEFAULT_INTERFACE_METHOD.getDeclaringClass()));
    }

    @Test
    @DisplayName(
        "should create a lambda which invokes the target method (default interface method)")
    void test3() throws Throwable {
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      @SuppressWarnings("unchecked")
      BiFunction<Object, Object, Object> lambda =
          LambdaFactory.createLambdaFunction(
              DEFAULT_INTERFACE_METHOD,
              BiFunction.class,
              DEFAULT_INTERFACE_METHOD.getDeclaringClass());

      String value = "test";
      String expected = instance.defaultMethod(value);
      Object result = lambda.apply(instance, value);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName(
        "should throw when special caller argument does not have access to the method e.g. "
            + "special caller is not a subclass of the method's declaring class")
    void test4() throws Throwable {
      // Caller is not a subclass of the method's declaring class.
      assertThrows(
          IllegalAccessException.class,
          () ->
              LambdaFactory.createLambdaFunction(
                  DEFAULT_INTERFACE_METHOD, BiFunction.class, NonDefaultMethodInterface.class));
    }
  }

  static interface DefaultMethodInterface {
    default String defaultMethod(String defaultValue) {
      return defaultValue;
    }
  }

  static interface NonDefaultMethodInterface {
    String nonDefaultMethod(String defaultValue);
  }
}
