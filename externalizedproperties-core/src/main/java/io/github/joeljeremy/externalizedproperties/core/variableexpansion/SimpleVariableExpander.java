package io.github.joeljeremy.externalizedproperties.core.variableexpansion;

import static io.github.joeljeremy.externalizedproperties.core.internal.Arguments.requireNonNullOrEmpty;

import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy.externalizedproperties.core.VariableExpander;

/**
 * A simple {@link VariableExpander} implementation. This resolves the variables from the resolver.
 *
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class SimpleVariableExpander implements VariableExpander {
  private static final String DEFAULT_VARIABLE_PREFIX = "${";
  private static final String DEFAULT_VARIABLE_END_SUFFIX = "}";

  private final String variablePrefix;
  private final String variableSuffix;

  /** Construct a string variable expander which looks up variable values from the resolver. */
  public SimpleVariableExpander() {
    this(DEFAULT_VARIABLE_PREFIX, DEFAULT_VARIABLE_END_SUFFIX);
  }

  /**
   * Construct a string variable expander which uses a custom variable prefix and suffix and looks
   * up variable values from the resolver.
   *
   * @param variablePrefix The variable prefix to look for when expanding variables.
   * @param variableSuffix The variable suffix to look for when expanding variables.
   */
  public SimpleVariableExpander(String variablePrefix, String variableSuffix) {
    this.variablePrefix = requireNonNullOrEmpty(variablePrefix, "variablePrefix");
    this.variableSuffix = requireNonNullOrEmpty(variableSuffix, "variableSuffix");
  }

  /** {@inheritDoc} */
  @Override
  public String expandVariables(InvocationContext context, String value) {
    if (value == null || value.isEmpty() || value.indexOf(variablePrefix) == -1) {
      return value;
    }

    try {
      return expandVariables(context, new StringBuilder(value)).toString();
    } catch (RuntimeException ex) {
      throw new VariableExpansionException(
          "Exception occurred while trying to expand value: " + value, ex);
    }
  }

  private StringBuilder expandVariables(InvocationContext context, StringBuilder builder) {
    int startIndex = builder.indexOf(variablePrefix);
    if (startIndex == -1) {
      return builder;
    }

    int variableNameStartIndex = startIndex + variablePrefix.length();
    int variableNameEndIndex = builder.indexOf(variableSuffix, variableNameStartIndex);
    if (variableNameEndIndex == -1 || variableNameStartIndex == variableNameEndIndex) {
      // No end tag or no variable name in between start and end tags.
      // e.g. "${test" or "${}"
      return builder;
    }

    String variableName = builder.substring(variableNameStartIndex, variableNameEndIndex);

    String variableValue = resolvePropertyValueOrThrow(context, variableName);

    builder.replace(startIndex, variableNameEndIndex + 1, variableValue);

    return expandVariables(context, builder);
  }

  private String resolvePropertyValueOrThrow(InvocationContext context, String variableName) {
    ResolverProxy resolverProxy = context.externalizedProperties().initialize(ResolverProxy.class);

    try {
      // Should throw if cannot be resolved.
      return resolverProxy.resolve(variableName);
    } catch (RuntimeException e) {
      throw new VariableExpansionException(
          "Failed to expand \""
              + variableName
              + "\" variable. "
              + "Variable value cannot be resolved.",
          e);
    }
  }

  private static interface ResolverProxy {
    @ResolverFacade
    public String resolve(String propertyName);
  }
}
