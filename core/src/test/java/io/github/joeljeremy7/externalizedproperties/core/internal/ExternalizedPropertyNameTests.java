package io.github.joeljeremy7.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy7.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ExternalizedPropertyNameTests {
  private static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().build();
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class FromInvocationContextMethod {
    @Test
    @DisplayName("should return the proxy method @ExternalizedProperty value")
    void test1() {
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      assertNotNull(name);
      assertEquals(
          context
              .method()
              .findAnnotation(ExternalizedProperty.class)
              .map(ExternalizedProperty::value)
              .orElse(null),
          name);
    }

    @Test
    @DisplayName(
        "should derive property name from proxy method invocation args value "
            + "when proxy method is annotated with @ResolverFacade")
    void test2() {
      String propertyNameArg = "property";
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::resolve, propertyNameArg, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      assertNotNull(name);
      assertEquals(propertyNameArg, name);
    }

    @Test
    @DisplayName(
        "should throw when proxy method is annotated with @ResolverFacade and there "
            + "is no property name provided via proxy method invocation args")
    void test3() {
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::resolve,
              null, // No property name invocation arguments
              EXTERNALIZED_PROPERTIES);

      assertThrows(
          IllegalArgumentException.class,
          () -> ExternalizedPropertyName.fromInvocationContext(context));
    }

    @Test
    @DisplayName(
        "should throw when proxy method is annotated with @ResolverFacade and the "
            + "property name provided via proxy method invocation args is null")
    void test4() {
      InvocationContext nullPropertyNameContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::resolve,
              null, // Null property name in invocation arguments
              EXTERNALIZED_PROPERTIES);

      assertThrows(
          IllegalArgumentException.class,
          () -> ExternalizedPropertyName.fromInvocationContext(nullPropertyNameContext));

      InvocationContext emptyPropertyNameContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::resolve,
              "", // Empty property name in invocation arguments
              EXTERNALIZED_PROPERTIES);

      assertThrows(
          IllegalArgumentException.class,
          () -> ExternalizedPropertyName.fromInvocationContext(emptyPropertyNameContext));
    }

    @Test
    @DisplayName(
        "should return method name when proxy method is neither annotated with "
            + "@ExternalizedProperty nor @ResolverFacade")
    void test5() {
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::noAnnotation, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      assertEquals(context.method().name(), name);
    }
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();

    @ResolverFacade
    String resolve(String propertyName);

    String noAnnotation();
  }
}
