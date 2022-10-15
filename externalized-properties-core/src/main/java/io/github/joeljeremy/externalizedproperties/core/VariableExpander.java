package io.github.joeljeremy.externalizedproperties.core;

/** The mechanism that allows expansion of variables in strings. */
public interface VariableExpander {
  /**
   * Expand any variables that is in the given string.
   *
   * @param context The proxy method invocation context.
   * @param value The string value. This may contain variables e.g.
   *     "${some.app.property}_property_name" which will be expanded by this method.
   * @return The string of which variables have been expanded.
   */
  String expandVariables(InvocationContext context, String value);
}
