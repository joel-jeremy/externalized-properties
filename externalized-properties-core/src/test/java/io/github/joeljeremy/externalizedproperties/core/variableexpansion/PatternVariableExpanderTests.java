package io.github.joeljeremy.externalizedproperties.core.variableexpansion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PatternVariableExpanderTests {
  static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().defaults().build();
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);
  // Variable pattern: #[variable]
  static final Pattern CUSTOM_VARIABLE_PATTERN = Pattern.compile("#\\[(.+?)\\]");

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when variable pattern argument is null")
    void test1() {
      assertThrows(IllegalArgumentException.class, () -> new PatternVariableExpander(null));
    }
  }

  @Nested
  class ExpandVariablesMethod {
    @Test
    @DisplayName("should return value when value is null or empty")
    void test1() {
      PatternVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyJavaVersion, EXTERNALIZED_PROPERTIES);

      String nullResult = variableExpander.expandVariables(context, null);
      String emptyResult = variableExpander.expandVariables(context, "");

      assertNull(nullResult);
      assertEquals("", emptyResult);
    }

    @Test
    @DisplayName("should expand variables with values from resolvers")
    void test2() {
      PatternVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyJavaVersion, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "property-${java.version}");

      ResolverFacadeProxyInterface resolverProxy =
          EXTERNALIZED_PROPERTIES.initialize(ResolverFacadeProxyInterface.class);

      String propertyValue = resolverProxy.resolve("java.version");

      assertEquals("property-" + propertyValue, result);
    }

    @Test
    @DisplayName("should return same string when there are no variables")
    void test3() {
      PatternVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNoVariables, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "property-no-variables");

      assertEquals("property-no-variables", result);
    }

    @Test
    @DisplayName("should throw when variable cannot be resolved from any resolvers")
    void test4() {
      PatternVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNonExistent, EXTERNALIZED_PROPERTIES);

      assertThrows(
          VariableExpansionException.class,
          () -> variableExpander.expandVariables(context, "property-${non.existent}"));
    }

    @Test
    @DisplayName("should expand variable with value from resolver using custom prefix and suffix")
    void test5() {
      PatternVariableExpander variableExpander = variableExpander(CUSTOM_VARIABLE_PATTERN);

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::customPrefixSuffix, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "property-#[java.version]");

      ResolverFacadeProxyInterface resolverProxy =
          EXTERNALIZED_PROPERTIES.initialize(ResolverFacadeProxyInterface.class);

      String propertyValue = resolverProxy.resolve("java.version");

      assertEquals("property-" + propertyValue, result);
    }
  }

  static PatternVariableExpander variableExpander() {
    return new PatternVariableExpander();
  }

  static PatternVariableExpander variableExpander(Pattern variablePattern) {
    return new PatternVariableExpander(variablePattern);
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property-${java.version}")
    String propertyJavaVersion();

    @ExternalizedProperty("property-no-variables")
    String propertyNoVariables();

    @ExternalizedProperty("property-${nonexistent}")
    String propertyNonExistent();

    @ExternalizedProperty("property-#[java.version]")
    String customPrefixSuffix();
  }

  static interface ResolverFacadeProxyInterface {
    @ResolverFacade
    String resolve(String propertyName);
  }
}
