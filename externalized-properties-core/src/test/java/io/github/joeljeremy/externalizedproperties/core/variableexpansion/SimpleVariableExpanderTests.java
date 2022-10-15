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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SimpleVariableExpanderTests {
  private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().defaults().build();

  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when variable prefix argument is null")
    void test2() {
      assertThrows(IllegalArgumentException.class, () -> new SimpleVariableExpander(null, "}"));
    }

    @Test
    @DisplayName("should throw when variable prefix argument is empty")
    void test3() {
      assertThrows(IllegalArgumentException.class, () -> new SimpleVariableExpander("", "}"));
    }

    @Test
    @DisplayName("should throw when variable suffix argument is null")
    void test4() {
      assertThrows(IllegalArgumentException.class, () -> new SimpleVariableExpander("${", null));
    }

    @Test
    @DisplayName("should throw when variable suffix argument is empty")
    void test5() {
      assertThrows(IllegalArgumentException.class, () -> new SimpleVariableExpander("${", ""));
    }
  }

  @Nested
  class ExpandVariablesMethod {
    @Test
    @DisplayName("should return value when value is null or empty")
    void test1() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyJavaVersion, EXTERNALIZED_PROPERTIES);

      String nullResult = variableExpander.expandVariables(context, null);
      String emptyResult = variableExpander.expandVariables(context, "");

      assertNull(nullResult);
      assertEquals("", emptyResult);
    }

    @Test
    @DisplayName("should expand variable with value from resolver")
    void test2() {
      SimpleVariableExpander variableExpander = variableExpander();

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
    @DisplayName("should expand multiple variables with values from resolvers")
    void test3() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyMultipleVariables, EXTERNALIZED_PROPERTIES);

      String result =
          variableExpander.expandVariables(context, "property-${java.version}-home-${java.home}");

      ResolverFacadeProxyInterface resolverProxy =
          EXTERNALIZED_PROPERTIES.initialize(ResolverFacadeProxyInterface.class);

      String javaVersionProperty = resolverProxy.resolve("java.version");
      String javaHomeProperty = resolverProxy.resolve("java.home");

      assertEquals("property-" + javaVersionProperty + "-home-" + javaHomeProperty, result);
    }

    @Test
    @DisplayName("should return original string when there are no variables")
    void test4() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNoVariables, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "property-no-variables");

      assertEquals("property-no-variables", result);
    }

    @Test
    @DisplayName("should throw when variable cannot be resolved from any resolvers")
    void test5() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNonExistent, EXTERNALIZED_PROPERTIES);

      assertThrows(
          VariableExpansionException.class,
          () -> variableExpander.expandVariables(context, "property-${non.existent}"));
    }

    @Test
    @DisplayName(
        "should skip expansion when there is no variable name between "
            + "variable prefix and variable suffix")
    void test6() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNoVariableName, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "test-${}");

      assertEquals("test-${}", result);
    }

    @Test
    @DisplayName(
        "should skip expansion "
            + "when there is there is a variable prefix detected but no variable suffix")
    void test7() {
      SimpleVariableExpander variableExpander = variableExpander();

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNoVariableSuffix, EXTERNALIZED_PROPERTIES);

      String result = variableExpander.expandVariables(context, "test-${variable");

      assertEquals("test-${variable", result);
    }

    @Test
    @DisplayName("should expand variable with value from resolver using custom prefix and suffix")
    void test8() {
      SimpleVariableExpander variableExpander = variableExpander("#[", "]");

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

  private static SimpleVariableExpander variableExpander() {
    return new SimpleVariableExpander();
  }

  private static SimpleVariableExpander variableExpander(
      String variablePrefix, String variableSuffix) {
    return new SimpleVariableExpander(variablePrefix, variableSuffix);
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property-${java.version}")
    String propertyJavaVersion();

    @ExternalizedProperty("property-${java.version}-home-${java.home}")
    String propertyMultipleVariables();

    @ExternalizedProperty("property-no-variables")
    String propertyNoVariables();

    @ExternalizedProperty("property-${nonexistent}")
    String propertyNonExistent();

    @ExternalizedProperty("property-#[java.version]")
    String customPrefixSuffix();

    @ExternalizedProperty("test-${}")
    String propertyNoVariableName();

    @ExternalizedProperty("test-${variable")
    String propertyNoVariableSuffix();
  }

  private static interface ResolverFacadeProxyInterface {
    @ResolverFacade
    String resolve(String propertyName);
  }
}
