package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.internal.DefaultInterfaceMethodHandlerFactory.DefaultInterfaceMethodHandler;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DefaultInterfaceMethodHandlerFactoryTests {
  static final Method TEST_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::test);

  static final Method TEST_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testOneArg);

  static final Method TEST_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testTwoArgs);

  static final Method TEST_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testThreeArgs);

  static final Method THROW_RUNTIME_EXCEPTION_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeException);

  static final Method THROW_RUNTIME_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionOneArg);

  static final Method THROW_RUNTIME_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionTwoArgs);

  static final Method THROW_RUNTIME_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionThreeArgs);

  static final Method THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, "throwException");

  static final Method THROW_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, "throwExceptionOneArg", String.class);

  static final Method THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, "throwExceptionTwoArgs", String.class, String.class);

  static final Method THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class,
          "throwExceptionThreeArgs",
          String.class,
          String.class,
          String.class);

  static final Method NON_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          NonDefaultMethodInterface.class, NonDefaultMethodInterface::nonDefaultMethod);

  @Nested
  class CreateMethod {
    @Test
    @DisplayName(
        "should throw if default interface method argument is not a default interface method")
    void test1() {
      assertThrows(
          IllegalArgumentException.class,
          () -> DefaultInterfaceMethodHandlerFactory.create(NON_DEFAULT_INTERFACE_METHOD));
    }

    @Test
    @DisplayName("should create a handler which invokes the target default interface method")
    void test2() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";
      // Invoke the method handle.
      String result = (String) handler.invoke(instance, value);

      assertEquals(value, result);
    }

    @Test
    @DisplayName(
        "should create a handler which invokes the target default interface method (with one"
            + " argument)")
    void test3() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(TEST_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";
      // Invoke the method handle.
      String result = (String) handler.invoke(instance, value);

      String expected = instance.testOneArg(value);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName(
        "should create a handler which invokes the target default interface method (with two"
            + " arguments)")
    void test4() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(TEST_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";
      // Invoke the method handle.
      String result = (String) handler.invoke(instance, value1, value2);

      String expected = instance.testTwoArgs(value1, value2);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName(
        "should create a handler which invokes the target default interface method (with two"
            + " arguments)")
    void test5() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(TEST_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";
      String value3 = "test3";
      // Invoke the method handle.
      String result = (String) handler.invoke(instance, value1, value2, value3);

      String expected = instance.testThreeArgs(value1, value2, value3);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName("should cache handlers per method")
    void test6() throws Throwable {
      DefaultInterfaceMethodHandler handler1 =
          DefaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);
      DefaultInterfaceMethodHandler handler2 =
          DefaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);

      assertSame(handler1, handler2);
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has no args.")
    void rethrowExceptionsTest1() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance));
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has one arg.")
    void rethrowExceptionsTest2() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance, value));
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has two args.")
    void rethrowExceptionsTest3() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";

      assertThrows(
          ExternalizedPropertiesException.class, () -> handler.invoke(instance, value1, value2));
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has three args.")
    void rethrowExceptionsTest4() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";
      String value3 = "test3";

      assertThrows(
          ExternalizedPropertiesException.class,
          () -> handler.invoke(instance, value1, value2, value3));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has no args.")
    void rethrowExceptionsTest5() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has one arg.")
    void rethrowExceptionsTest6() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance, value));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has two args.")
    void rethrowExceptionsTest7() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";

      assertThrows(
          ExternalizedPropertiesException.class, () -> handler.invoke(instance, value1, value2));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has three args.")
    void rethrowExceptionsTest8() throws Throwable {
      DefaultInterfaceMethodHandler handler =
          DefaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class instance.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";
      String value3 = "test3";

      assertThrows(
          ExternalizedPropertiesException.class,
          () -> handler.invoke(instance, value1, value2, value3));
    }
  }

  static interface DefaultMethodInterface {
    default String test() {
      return "test";
    }

    default String testOneArg(String defaultValue) {
      return defaultValue;
    }

    default String testTwoArgs(String arg1, String arg2) {
      return arg1 != null ? arg1 : arg2;
    }

    default String testThreeArgs(String arg1, String arg2, String arg3) {
      return arg1 != null ? arg2 : arg3;
    }

    default String throwRuntimeException() {
      throw new RuntimeException("Oops!");
    }

    // Used by THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD.
    default String throwException() throws Exception {
      throw new Exception("Oops!");
    }

    default String throwRuntimeExceptionOneArg(String arg1) {
      throw new RuntimeException("Oops!");
    }

    // Used by THROW_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD.
    default String throwExceptionOneArg(String arg1) throws Exception {
      throw new Exception("Oops!");
    }

    default String throwRuntimeExceptionTwoArgs(String arg1, String arg2) {
      throw new RuntimeException("Oops!");
    }

    default String throwRuntimeExceptionThreeArgs(String arg1, String arg2, String arg3) {
      throw new RuntimeException("Oops!");
    }

    // Used by THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD.
    default String throwExceptionTwoArgs(String arg1, String arg2) throws Exception {
      throw new Exception("Oops!");
    }

    // Used by THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD.
    default String throwExceptionThreeArgs(String arg1, String arg2, String arg3) throws Exception {
      throw new Exception("Oops!");
    }
  }

  static interface NonDefaultMethodInterface {
    String nonDefaultMethod(String defaultValue);
  }
}
