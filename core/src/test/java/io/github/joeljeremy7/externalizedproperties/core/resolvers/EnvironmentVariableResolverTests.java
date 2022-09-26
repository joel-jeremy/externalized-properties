package io.github.joeljeremy7.externalizedproperties.core.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.testentities.Unsafe;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EnvironmentVariableResolverTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve property value from environment variables")
    void test1() {
      EnvironmentVariableResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::path, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "PATH");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(System.getenv("PATH"), result.get());
    }

    @Test
    @DisplayName("should return empty Optional when environment variable is not found")
    void test2() {
      EnvironmentVariableResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notFound, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "not.found");

      assertNotNull(result);
      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
        "should attempt to resolve environment variable by formatting "
            + "property name to environment variable format (convert dots to underscores)")
    void test3() {
      Unsafe.setEnv("TEST_3", "test3");
      try {
        EnvironmentVariableResolver resolver = resolverToTest();
        InvocationContext context =
            INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::test3, externalizedProperties(resolver));

        Optional<String> result =
            resolver.resolve(
                context,
                // test.3 should be converted to TEST_3
                "test.3");

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(System.getenv("TEST_3"), result.get());
      } finally {
        Unsafe.clearEnv("TEST_3");
      }
    }

    @Test
    @DisplayName(
        "should attempt to resolve environment variable by formatting "
            + "property name to environment variable format (convert dashes to underscores)")
    void test4() {
      Unsafe.setEnv("TEST_4", "test4");
      try {
        EnvironmentVariableResolver resolver = resolverToTest();
        InvocationContext context =
            INVOCATION_CONTEXT_FACTORY.fromMethodReference(
                ProxyInterface::test4, externalizedProperties(resolver));

        Optional<String> result =
            resolver.resolve(
                context,
                // test-4 should be converted to TEST_ENV_VAR
                "test-4");

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(System.getenv("TEST_4"), result.get());
      } finally {
        Unsafe.clearEnv("TEST_4");
      }
    }
  }

  private static EnvironmentVariableResolver resolverToTest() {
    return new EnvironmentVariableResolver();
  }

  private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  private static interface ProxyInterface {
    /**
     * EnvironmentVariableResolver supports formatting of property names such that "path" is
     * converted to "PATH".
     */
    @ExternalizedProperty("path")
    String path();

    /**
     * EnvironmentVariableResolver supports formatting of property names such that "test.3" is
     * converted to "TEST_3".
     */
    @ExternalizedProperty("test.3")
    String test3();

    /**
     * EnvironmentVariableResolver supports formatting of property names such that "test-4" is
     * converted to "TEST_4".
     */
    @ExternalizedProperty("test-4")
    String test4();

    @ExternalizedProperty("not.found")
    String notFound();
  }
}
