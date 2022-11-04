package io.github.joeljeremy.externalizedproperties.core.variableexpansion;

import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class NoOpVariableExpanderTests {
  static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().defaults().build();
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class ExpandVariablesMethod {
    @Test
    @DisplayName("should just return the input value")
    void test1() {
      String value = "${test}";

      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::variableProperty, EXTERNALIZED_PROPERTIES);

      String result = NoOpVariableExpander.INSTANCE.expandVariables(context, value);

      assertSame(value, result);
    }
  }

  static interface ProxyInterface {
    @ExternalizedProperty("${test}")
    String variableProperty();
  }
}
