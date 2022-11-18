package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.testfixtures.MethodUtils;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MethodHandleFactoryTests {
  static final Method DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(DefaultMethodInterface.class, DefaultMethodInterface::defaultMethod);

  static final Method NON_DEFAULT_INTERFACE_METHOD =
      MethodUtils.getMethod(
          NonDefaultMethodInterface.class, NonDefaultMethodInterface::nonDefaultMethod);

  @Nested
  class MethodHandleForMethod {
    @Test
    @DisplayName(
        "should create a method handle which invokes the target method (default interface method)")
    void test1() throws Throwable {
      MethodHandle methodHandle = MethodHandleFactory.methodHandleFor(DEFAULT_INTERFACE_METHOD);

      DefaultMethodInterface instance = new DefaultMethodInterface() {};

      String value = "test";
      String expected = instance.defaultMethod(value);
      String result = (String) methodHandle.invokeExact(instance, value);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName("should create a method handle which invokes the target method")
    void test2() throws Throwable {
      MethodHandle methodHandle = MethodHandleFactory.methodHandleFor(NON_DEFAULT_INTERFACE_METHOD);

      NonDefaultMethodInterface instance =
          new NonDefaultMethodInterface() {
            @Override
            public String nonDefaultMethod(String defaultValue) {
              return defaultValue;
            }
          };

      String value = "test";
      String expected = instance.nonDefaultMethod(value);
      String actual = (String) methodHandle.invokeExact(instance, value);

      assertEquals(expected, actual);
    }
  }

  @Nested
  class MethodHandleForMethodWithSpecialCallerOverload {
    @Test
    @DisplayName("should throw when target method is not a default interface method")
    void test1() throws Throwable {
      Class<?> specialCaller = NON_DEFAULT_INTERFACE_METHOD.getDeclaringClass();
      assertThrows(
          AbstractMethodError.class,
          () -> MethodHandleFactory.methodHandleFor(NON_DEFAULT_INTERFACE_METHOD, specialCaller));
    }

    @Test
    @DisplayName(
        "should create a method handle which invokes the target method (default interface method)")
    void test2() throws Throwable {
      DefaultMethodInterface instance = new DefaultMethodInterface() {};
      Class<?> specialCaller = DEFAULT_INTERFACE_METHOD.getDeclaringClass();

      MethodHandle methodHandle =
          MethodHandleFactory.methodHandleFor(DEFAULT_INTERFACE_METHOD, specialCaller);

      String value = "test";
      String expected = instance.defaultMethod(value);
      String result = (String) methodHandle.invokeExact(instance, value);

      assertEquals(expected, result);
    }

    @Test
    @DisplayName(
        "should throw when special caller argument does not have access to the method e.g. "
            + "special caller is not a subclass of the method's declaring class")
    void test3() throws Throwable {
      // Caller is not a subclass of the method's declaring class.
      Class<?> specialCaller = NonDefaultMethodInterface.class;
      assertThrows(
          IllegalAccessException.class,
          () -> MethodHandleFactory.methodHandleFor(DEFAULT_INTERFACE_METHOD, specialCaller));
    }
  }

  @Nested
  class PrivateLookupInMethod {
    @Test
    @DisplayName("should return private lookup for target class")
    void test1() throws Throwable {
      Lookup lookup = MethodHandleFactory.privateLookupIn(DefaultMethodInterface.class);

      assertNotNull(lookup);

      // Assert lookupModes has Lookup.PRIVATE flag.
      int lookupModes = lookup.lookupModes();
      // Same check as Lookup.hasPrivateAccess() introduced in Java 9.
      boolean hasPrivateAccess = (lookupModes & Lookup.PRIVATE) != 0;
      assertTrue(hasPrivateAccess);
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
