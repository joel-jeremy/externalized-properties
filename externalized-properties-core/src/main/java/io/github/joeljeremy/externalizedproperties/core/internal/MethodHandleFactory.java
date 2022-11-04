package io.github.joeljeremy.externalizedproperties.core.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
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
   * @throws Throwable if an exception occurred while building the method handle.
   */
  public static MethodHandle methodHandleFor(Method method) throws Throwable {
    return methodHandleFor(method, null);
  }

  /**
   * Get method handle.
   *
   * @param method The method to build a method handle for.
   * @param specialCaller The class nominally calling the method or {@code null} if there is no
   *     special caller.
   * @return The method handle.
   * @throws Throwable if an exception occurred while building the method handle.
   */
  public static MethodHandle methodHandleFor(Method method, @Nullable Class<?> specialCaller)
      throws Throwable {
    if (JvmVersion.isJava9OrLater()) {
      return Java9MethodHandleFactory.methodHandleFor(method, specialCaller);
    }

    return Java8MethodHandleFactory.methodHandleFor(method, specialCaller);
  }

  /**
   * Private lookup.
   *
   * @param classToLookup The class to lookup.
   * @return The {@code Lookup} instance.
   * @throws Throwable if an exception occurred while retrieving the {@code Lookup} instance.
   */
  public static Lookup privateLookupIn(Class<?> classToLookup) throws Throwable {
    if (JvmVersion.isJava9OrLater()) {
      return Java9MethodHandleFactory.privateLookupIn(classToLookup);
    }

    return Java8MethodHandleFactory.privateLookupIn(classToLookup);
  }

  private static class Java8MethodHandleFactory {
    private Java8MethodHandleFactory() {}

    /**
     * Get method handle.
     *
     * @param method The method to build a method handle for.
     * @param specialCaller The class nominally calling the method or {@code null} if there is no
     *     special caller.
     * @return The method handle.
     * @throws Throwable if an exception occurred while building the method handle.
     */
    private static MethodHandle methodHandleFor(Method method, @Nullable Class<?> specialCaller)
        throws Throwable {
      if (specialCaller != null) {
        return privateLookupIn(specialCaller).unreflectSpecial(method, specialCaller);
      }
      return privateLookupIn(method.getDeclaringClass()).unreflect(method);
    }

    /**
     * Private lookup.
     *
     * @param classToLookup The class to lookup.
     * @return The {@code Lookup} instance.
     * @throws Throwable if an exception occurred while retrieving the {@code Lookup} instance.
     */
    private static Lookup privateLookupIn(Class<?> classToLookup) throws Throwable {
      // This will only work on Java 8.
      // For Java9+, the new private lookup API should be used.
      final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);

      try {
        constructor.setAccessible(true);
        return constructor.newInstance(classToLookup);
      } finally {
        constructor.setAccessible(false);
      }
    }
  }

  private static class Java9MethodHandleFactory {
    // This will only work on Java 9+.
    // This method should be present in Java 9+.
    // Method handle for MethodHandles.privateLookupIn(...) method.
    private static final MethodHandle JAVA_9_MH_PRIVATE_LOOKUP_IN =
        privateLookupInMethodHandleOrThrow();

    private Java9MethodHandleFactory() {}

    /**
     * Get method handle.
     *
     * @param method The method to build a method handle for.
     * @param specialCaller The class nominally calling the method or {@code null} if there is no
     *     special caller.
     * @return The method handle.
     * @throws Throwable if an exception occurred while building the method handle.
     */
    private static MethodHandle methodHandleFor(Method method, @Nullable Class<?> specialCaller)
        throws Throwable {
      if (specialCaller != null) {
        return privateLookupIn(specialCaller).unreflectSpecial(method, specialCaller);
      }
      return privateLookupIn(method.getDeclaringClass()).unreflect(method);
    }

    /**
     * Private lookup.
     *
     * @param classToLookup The class to lookup.
     * @return The {@code Lookup} instance.
     * @throws Throwable if an exception occurred while retrieving the {@code Lookup} instance.
     */
    private static Lookup privateLookupIn(Class<?> classToLookup) throws Throwable {
      return (Lookup)
          JAVA_9_MH_PRIVATE_LOOKUP_IN.invokeExact(classToLookup, MethodHandles.lookup());
    }

    private static MethodHandle privateLookupInMethodHandleOrThrow() {
      try {
        Method privateLookupIn =
            MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
        return MethodHandles.lookup().unreflect(privateLookupIn);
      } catch (Exception e) {
        throw new IllegalStateException(
            "Unable to find MethodHandles.privateLookupIn method "
                + "while running on Java "
                + JvmVersion.version()
                + ".",
            e);
      }
    }
  }

  private static class JvmVersion {
    // Not Java 1.7, 1.8, etc.
    private static final boolean IS_RUNNING_ON_JAVA_9_OR_LATER = !version().startsWith("1.");

    private JvmVersion() {}

    /**
     * Check if JVM version is 9 or later.
     *
     * @return {@code true} when the detected JVM version is 9 or greater. Otherwise, {@code false}.
     */
    public static boolean isJava9OrLater() {
      return IS_RUNNING_ON_JAVA_9_OR_LATER;
    }

    /**
     * The JVM version as defined by the {@code java.specification.version}system property.
     *
     * @return The JVM version as defined by the {@code java.specification.version} system property.
     */
    public static String version() {
      return System.getProperty("java.specification.version");
    }
  }
}
