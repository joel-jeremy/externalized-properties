package io.github.joeljeremy.externalizedproperties.core.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class SystemPropertyResolverTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve property value from system properties")
    void test1() {
      SystemPropertyResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::javaVersion, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "java.version");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(System.getProperty("java.version"), result.get());
    }

    @Test
    @DisplayName("should return empty Optional when system property is not found")
    void test2() {
      SystemPropertyResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notFound, externalizedProperties(resolver));

      Optional<String> result =
          resolver.resolve(
              context, "not.found" // Not in system properties.
              );

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  static SystemPropertyResolver resolverToTest() {
    return new SystemPropertyResolver();
  }

  static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();

    @ExternalizedProperty("not.found")
    String notFound();
  }
}
