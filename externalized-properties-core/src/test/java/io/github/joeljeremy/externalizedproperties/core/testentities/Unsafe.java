package io.github.joeljeremy.externalizedproperties.core.testentities;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Unsafe utilities.
 *
 * <p>
 *
 * <h2>DO NOT USE THIS OUTSIDE OF TESTING PURPOSES. </h2>
 */
public class Unsafe {

  private Unsafe() {}

  /**
   * Set an environment variable.
   *
   * <p>
   *
   * <h2>DO NOT USE THIS OUTSIDE OF TESTING PURPOSES. Future java releases may break this method's
   * assumptions and introduce a bug. </h2>
   *
   * @param name The environment variable name.
   * @param value The environment variable value.
   */
  public static void setEnv(String name, String value) {
    try {
      getMutableEnvironmentVariables().put(name, value);
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Exception occurred while trying to obtain the mutable " + "environment variables map.",
          ex);
    }
  }

  /**
   * Clear an environment variable.
   *
   * <p>
   *
   * <h2>DO NOT USE THIS OUTSIDE OF TESTING PURPOSES. Future java releases may break this method's
   * assumptions and introduce a bug. </h2>
   *
   * @param name The environment variable name.
   */
  public static void clearEnv(String name) {
    try {
      getMutableEnvironmentVariables().remove(name);
    } catch (Exception ex) {
      throw new IllegalStateException(
          "Exception occurred while trying to obtain the mutable "
              + "environment variables map and clear an environment variable.",
          ex);
    }
  }

  /**
   * Hack to be able to mutate the system environment variables. This reflectively gets the internal
   * mutable map representation from the {@link System#getenv}'s unmodifiable map. The resulting map
   * is a mutable map where any modifications will reflect in any succeeding {@link System#getenv}
   * invocations.
   *
   * <p>
   *
   * <h2>DO NOT USE THIS OUTSIDE OF TESTING PURPOSES. Future java releases may break this method's
   * assumptions and introduce a bug. </h2>
   */
  @SuppressWarnings("unchecked")
  private static Map<String, String> getMutableEnvironmentVariables() throws Exception {
    // This is an Collections$UnmodifiableMap. We need to extract the internal
    // map representation to be able to set environment variables.
    Map<String, String> env = System.getenv();
    // The m field in Collections$UnmodifiableMap.
    Field mapField = env.getClass().getDeclaredField("m");
    mapField.setAccessible(true);
    return (Map<String, String>) mapField.get(env);
  }
}
