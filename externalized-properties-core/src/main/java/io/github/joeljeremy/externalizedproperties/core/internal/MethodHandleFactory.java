package io.github.joeljeremy.externalizedproperties.core.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import org.checkerframework.checker.nullness.qual.Nullable;

/** The {@code MethodHandle} factory. */
@Internal
public class MethodHandleFactory {
  private MethodHandleFactory() {}
  /**
   * Get method handle.
   *
   * @param method The method to build a method handle for.
   * @return The method handle.
   * @throws IllegalAccessException if Java access check failed.
   */
  public static MethodHandle methodHandleFor(Method method) throws IllegalAccessException {
    return methodHandleFor(method, null);
  }

  /**
   * Get a method handle for the specified method.
   *
   * @param method The method to build a method handle for.
   * @param specialCaller The class nominally calling the method or {@code null} if there is no
   *     special caller.
   * @return The method handle.
   * @throws IllegalAccessException if Java access check failed.
   */
  public static MethodHandle methodHandleFor(Method method, @Nullable Class<?> specialCaller)
      throws IllegalAccessException {
    MethodHandles.Lookup lookup =
        MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup());
    if (specialCaller != null) {
      return lookup.unreflectSpecial(method, specialCaller);
    }
    return lookup.unreflect(method);
  }
}
