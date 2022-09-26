package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.StubResolver;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class VariableExpandingResolverTests {
  private static final InvocationContextTestFactory<ProxyInterface>
      INVOCATION_CONTEXT_TEST_FACTORY = InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when decorated argument is null")
    void test1() {
      assertThrows(IllegalArgumentException.class, () -> new VariableExpandingResolver(null));
    }
  }

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should expand variables in resolved property")
    void test1() {
      Map<String, String> source = new HashMap<>();
      source.put("property", "${variable}");
      source.put("variable", "variable-value");

      VariableExpandingResolver resolver = new VariableExpandingResolver(new MapResolver(source));

      InvocationContext context =
          INVOCATION_CONTEXT_TEST_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> resolved = resolver.resolve(context, "property");

      assertTrue(resolved.isPresent());
      // ${variable} should get expanded.
      assertEquals(source.get("variable"), resolved.get());
    }

    @Test
    @DisplayName("should not modify resolved property when there are no variables")
    void test2() {
      StubResolver decorated = new StubResolver();
      VariableExpandingResolver resolver = new VariableExpandingResolver(decorated);

      InvocationContext context =
          INVOCATION_CONTEXT_TEST_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> resolved = resolver.resolve(context, "property");

      assertTrue(resolved.isPresent());
      assertEquals(decorated.valueResolver().apply("property"), resolved.get());
    }
  }

  private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder()
        .enableVariableExpansionInProperties()
        .resolvers(resolvers)
        .build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();
  }
}
