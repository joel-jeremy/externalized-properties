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

public class DefaultResolverTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve property value from system properties")
    void systemPropertyTest1() {
      DefaultResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::javaVersion, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "java.version");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(System.getProperty("java.version"), result.get());
    }

    @Test
    @DisplayName("should resolve property value from environment variables")
    void environmentVariableTest1() {
      DefaultResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::path, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "path");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(System.getenv("PATH"), result.get());
    }

    @Test
    @DisplayName(
        "should return empty Optional when property cannot be found "
            + "in any of the default resolvers.")
    void notFoundTest1() {
      DefaultResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notFound, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "not.found");

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  private static DefaultResolver resolverToTest() {
    return new DefaultResolver();
  }

  private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();

    @ExternalizedProperty("path")
    String path();

    @ExternalizedProperty("not.found")
    String notFound();
  }
}
