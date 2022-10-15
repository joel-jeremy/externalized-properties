package io.github.joeljeremy.externalizedproperties.core.resolvers;

import java.util.Arrays;

/**
 * Default property converter which delegates to the following resolvers (in order):
 *
 * <ol>
 *   <li>{@link SystemPropertyResolver}
 *   <li>{@link EnvironmentVariableResolver}
 * </ol>
 */
public class DefaultResolver extends CompositeResolver {
  /**
   * Construct a property converter which delegates to the following resolvers (in order):
   *
   * <ol>
   *   <li>{@link SystemPropertyResolver}
   *   <li>{@link EnvironmentVariableResolver}
   * </ol>
   */
  public DefaultResolver() {
    super(Arrays.asList(new SystemPropertyResolver(), new EnvironmentVariableResolver()));
  }
}
