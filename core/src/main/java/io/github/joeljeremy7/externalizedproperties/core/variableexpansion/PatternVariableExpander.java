package io.github.joeljeremy7.externalizedproperties.core.variableexpansion;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A regex/pattern-based {@link VariableExpander} implementation. This resolves the variables from
 * the resolver.
 *
 * @implNote By default, this will match the basic pattern: ${variable}
 */
public class PatternVariableExpander implements VariableExpander {
  /** Pattern: ${variable} */
  private static final Pattern DEFAULT_VARIABLE_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

  private final Pattern variablePattern;

  /** Constructor. */
  public PatternVariableExpander() {
    this(DEFAULT_VARIABLE_PATTERN);
  }

  /**
   * Constructor.
   *
   * @param variablePattern The pattern to look for when looking for variables to expand.
   */
  public PatternVariableExpander(Pattern variablePattern) {
    this.variablePattern = requireNonNull(variablePattern, "variablePattern");
  }

  /** {@inheritDoc} */
  @Override
  public String expandVariables(InvocationContext context, String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }

    try {
      return replaceVariables(context, value);
    } catch (RuntimeException ex) {
      throw new VariableExpansionException(
          "Exception occurred while trying to expand value: " + value, ex);
    }
  }

  private String replaceVariables(InvocationContext context, String value) {
    StringBuffer output = new StringBuffer();
    Matcher matcher = variablePattern.matcher(value);

    while (matcher.find()) {
      // Resolve property from variable.
      String propertyNameVariable = matcher.group(1);
      String propertyValue = resolvePropertyValueOrThrow(context, propertyNameVariable);
      matcher.appendReplacement(output, propertyValue);
    }

    // Append any text after the variable if there are any.
    return matcher.appendTail(output).toString();
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
              + "Variable value cannot be resolved from the resolver.",
          e);
    }
  }

  private static interface ResolverProxy {
    @ResolverFacade
    String resolve(String propertyName);
  }
}
