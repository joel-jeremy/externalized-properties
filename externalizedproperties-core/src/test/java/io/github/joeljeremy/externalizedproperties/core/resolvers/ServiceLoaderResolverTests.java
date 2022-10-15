package io.github.joeljeremy.externalizedproperties.core.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

/** Service loader resolvers are configured in resources/META-INF/services folder. */
public class ServiceLoaderResolverTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should load properties from ServiceLoader resolvers")
    void test1() {
      ServiceLoaderResolver resolver = resolverToTest();
      InvocationContext javaVersionContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::javaVersion, externalizedProperties(resolver));
      InvocationContext pathContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::path, externalizedProperties(resolver));

      Optional<String> javaVersion = resolver.resolve(javaVersionContext, "java.version");
      Optional<String> pathEnv = resolver.resolve(pathContext, "path");

      // From SystemPropertyResolver.
      assertNotNull(javaVersion);
      assertEquals(System.getProperty("java.version"), javaVersion.get());

      // From EnvironmentVariableResolver.
      assertNotNull(pathEnv);
      assertEquals(System.getenv("PATH"), pathEnv.get());
    }

    @Test
    @DisplayName(
        "should return empty Optional when property cannot be resolved from "
            + "any of the ServiceLoader resolvers")
    void test2() {
      ServiceLoaderResolver resolver = resolverToTest();
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::notFound, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "non.found");

      assertNotNull(result);
      assertFalse(result.isPresent());
    }
  }

  private static ServiceLoaderResolver resolverToTest() {
    return new ServiceLoaderResolver();
  }

  private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("java.version")
    String javaVersion();

    @ExternalizedProperty("path")
    String path();

    @ExternalizedProperty("property")
    String notFound();
  }
}
