package io.github.joeljeremy.externalizedproperties.core.internal;

import static io.github.joeljeremy.externalizedproperties.core.internal.DefaultInterfaceMethodHandlerFactory.LambdaFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy.externalizedproperties.core.internal.DefaultInterfaceMethodHandlerFactory.DefaultInterfaceMethodHandler;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.awt.List;
import java.lang.reflect.Method;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DefaultInterfaceMethodHandlerFactoryTests {
  private final Method TEST_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::test);

  private final Method TEST_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testOneArg);

  private final Method TEST_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testTwoArgs);

  private final Method TEST_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::testThreeArgs);

  private final Method THROW_RUNTIME_EXCEPTION_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeException);

  private final Method THROW_RUNTIME_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionOneArg);

  private final Method THROW_RUNTIME_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionTwoArgs);

  private final Method THROW_RUNTIME_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, DefaultMethodInterface::throwRuntimeExceptionThreeArgs);

  private final Method THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, "throwException");

  private final Method THROW_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, "throwExceptionOneArg", String.class);

  private final Method THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class, "throwExceptionTwoArgs", String.class, String.class);

  private final Method THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          DefaultMethodInterface.class,
          "throwExceptionThreeArgs",
          String.class,
          String.class,
          String.class);

  private final Method NON_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          NonDefaultMethodInterface.class, NonDefaultMethodInterface::nonDefaultMethod);

  @Nested
  class CreateMethod {
    @Test
    @DisplayName(
        "should throw if default interface method argument is not a default interface method")
    void test1() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();

      assertThrows(
          IllegalArgumentException.class,
          () -> defaultInterfaceMethodHandlerFactory.create(NON_DEFAULT_INTERFACE_METHOD));
    }

    @Test
    @DisplayName("should create a handler which invokes the target default interface method")
    void test2() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void test3() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(TEST_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void test4() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(TEST_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void test5() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(TEST_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void test6() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler1 =
          defaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);
      DefaultInterfaceMethodHandler handler2 =
          defaultInterfaceMethodHandlerFactory.create(TEST_DEFAULT_INTERFACE_METHOD);

      assertSame(handler1, handler2);
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has no args.")
    void rethrowExceptionsTest1() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance));
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has one arg.")
    void rethrowExceptionsTest2() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance, value));
    }

    @Test
    @DisplayName(
        "should create handler that wraps runtime exceptions thrown by default interface method. "
            + "Method has two args.")
    void rethrowExceptionsTest3() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void rethrowExceptionsTest4() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_RUNTIME_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void rethrowExceptionsTest5() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has one arg.")
    void rethrowExceptionsTest6() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_ONE_ARG_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";

      assertThrows(ExternalizedPropertiesException.class, () -> handler.invoke(instance, value));
    }

    @Test
    @DisplayName(
        "should create handler that wraps checked exceptions thrown by default interface method. "
            + "Method has two args.")
    void rethrowExceptionsTest7() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
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
    void rethrowExceptionsTest8() {
      DefaultInterfaceMethodHandlerFactory defaultInterfaceMethodHandlerFactory =
          new DefaultInterfaceMethodHandlerFactory();
      DefaultInterfaceMethodHandler handler =
          defaultInterfaceMethodHandlerFactory.create(
              THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD);

      // Anonymous class.
      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value1 = "test1";
      String value2 = "test2";
      String value3 = "test3";

      assertThrows(
          ExternalizedPropertiesException.class,
          () -> handler.invoke(instance, value1, value2, value3));
    }
  }

  @Nested
  class LambdaFactoryTests {
    @Nested
    class CreateLambdaFunctionMethod {
      @Test
      @DisplayName("should throw when target method argument is not a default interface method")
      void test1() {
        assertThrows(
            IllegalArgumentException.class,
            () -> LambdaFactory.createLambdaFunction(NON_DEFAULT_INTERFACE_METHOD, Function.class));
      }

      @Test
      @DisplayName("should throw when functional interface argument is not a functional interface")
      void test2() {
        assertThrows(
            IllegalArgumentException.class,
            () -> LambdaFactory.createLambdaFunction(TEST_DEFAULT_INTERFACE_METHOD, List.class));
      }
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

    @SuppressWarnings("unused")
    // Used by THROW_EXCEPTION_DEFAULT_INTERFACE_METHOD.
    default String throwException() throws Exception {
      throw new Exception("Oops!");
    }

    default String throwRuntimeExceptionOneArg(String arg1) {
      throw new RuntimeException("Oops!");
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    // Used by THROW_EXCEPTION_TWO_ARGS_DEFAULT_INTERFACE_METHOD.
    default String throwExceptionTwoArgs(String arg1, String arg2) throws Exception {
      throw new Exception("Oops!");
    }

    @SuppressWarnings("unused")
    // Used by THROW_EXCEPTION_THREE_ARGS_DEFAULT_INTERFACE_METHOD.
    default String throwExceptionThreeArgs(String arg1, String arg2, String arg3) throws Exception {
      throw new Exception("Oops!");
    }
  }

  private static interface NonDefaultMethodInterface {
    String nonDefaultMethod(String defaultValue);
  }
}
