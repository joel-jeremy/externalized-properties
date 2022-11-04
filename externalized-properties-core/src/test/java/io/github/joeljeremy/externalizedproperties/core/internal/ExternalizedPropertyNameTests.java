package io.github.joeljeremy.externalizedproperties.core.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedPropertyPrefix;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

public class ExternalizedPropertyNameTests {
  static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().build();

  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  static final InvocationContextTestFactory<PrefixProxyInterface>
      PREFIX_INVOCATION_CONTEXT_FACTORY =
          InvocationContextUtils.testFactory(PrefixProxyInterface.class);

  static final InvocationContextTestFactory<PrefixWithDelimiterProxyInterface>
      PREFIX_WITH_DELIMITER_INVOCATION_CONTEXT_FACTORY =
          InvocationContextUtils.testFactory(PrefixWithDelimiterProxyInterface.class);

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    @DisplayName(
        "should throw when proxy method is annotated with @ResolverFacade and the "
            + "property name provided via proxy method invocation args is null, empty, or blank")
    void test3(String propertyNameArg) {
      InvocationContext nullPropertyNameContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::resolve, propertyNameArg, EXTERNALIZED_PROPERTIES);

      assertThrows(
          IllegalArgumentException.class,
          () -> ExternalizedPropertyName.fromInvocationContext(nullPropertyNameContext));
    }

    @Test
    @DisplayName(
        "should return method name when proxy method is neither annotated with "
            + "@ExternalizedProperty nor @ResolverFacade")
    void test4() {
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::noAnnotation, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      assertEquals(context.method().name(), name);
    }

    // Prefix tests

    @Test
    @DisplayName(
        "should prefix externalized property name when proxy method's declaring class is annotated"
            + " with @ExternalizedPropertyPrefix (externalized property name was derived from"
            + " @ExternalizedProperty)")
    void test5() {
      InvocationContext context =
          PREFIX_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixProxyInterface::property, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      ExternalizedProperty externalizedProperty =
          context
              .method()
              .findAnnotation(ExternalizedProperty.class)
              .orElseThrow(() -> new AssertionFailedError("No @ExternalizedProperty annotation."));

      String expected = prefix.value() + prefix.delimiter() + externalizedProperty.value();

      assertEquals(expected, name);
    }

    @Test
    @DisplayName(
        "should prefix externalized property name when proxy method's declaring class is annotated"
            + " with @ExternalizedPropertyPrefix (externalized property name was derived from"
            + " @ResolverFacade)")
    void test6() {
      String propertyName = "test.property";

      InvocationContext context =
          PREFIX_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixProxyInterface::resolve, propertyName, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      String expected = prefix.value() + prefix.delimiter() + propertyName;

      assertEquals(expected, name);
    }

    @Test
    @DisplayName(
        "should prefix externalized property name when proxy method's declaring class is annotated"
            + " with @ExternalizedPropertyPrefix (externalized property name was derived from"
            + " method name)")
    void test7() {
      InvocationContext context =
          PREFIX_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixProxyInterface::noAnnotation, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      String expected = prefix.value() + prefix.delimiter() + context.method().name();

      assertEquals(expected, name);
    }

    // Prefix with delimiter tests

    @Test
    @DisplayName(
        "should use @ExternalizedPropertyPrefix delimiter to delimit prefix and externalized"
            + " property name (externalized property name was derived from @ExternalizedProperty)")
    void test8() {
      InvocationContext context =
          PREFIX_WITH_DELIMITER_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixWithDelimiterProxyInterface::property, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      ExternalizedProperty externalizedProperty =
          context
              .method()
              .findAnnotation(ExternalizedProperty.class)
              .orElseThrow(() -> new AssertionFailedError("No @ExternalizedProperty annotation"));

      String expected = prefix.value() + prefix.delimiter() + externalizedProperty.value();

      assertEquals(expected, name);
    }

    @Test
    @DisplayName(
        "should use @ExternalizedPropertyPrefix delimiter to delimit prefix and externalized"
            + " property name (externalized property name was derived from @ResolverFacade)")
    void test9() {
      String propertyName = "test.property";

      InvocationContext context =
          PREFIX_WITH_DELIMITER_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixWithDelimiterProxyInterface::resolve, propertyName, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      String expected = prefix.value() + prefix.delimiter() + propertyName;

      assertEquals(expected, name);
    }

    @Test
    @DisplayName(
        "should use @ExternalizedPropertyPrefix delimiter to delimit prefix and externalized"
            + " property name (externalized property name was derived from method name)")
    void test10() {
      InvocationContext context =
          PREFIX_WITH_DELIMITER_INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              PrefixWithDelimiterProxyInterface::noAnnotation, EXTERNALIZED_PROPERTIES);

      String name = ExternalizedPropertyName.fromInvocationContext(context);

      ExternalizedPropertyPrefix prefix =
          context.method().declaringClass().getAnnotation(ExternalizedPropertyPrefix.class);

      String expected = prefix.value() + prefix.delimiter() + context.method().name();

      assertEquals(expected, name);
    }
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();

    @ResolverFacade
    String resolve(String propertyName);

    String noAnnotation();
  }

  @ExternalizedPropertyPrefix("myprefix")
  static interface PrefixProxyInterface {
    @ExternalizedProperty("test.property")
    String property();

    @ResolverFacade
    String resolve(String propertyName);

    String noAnnotation();
  }

  @ExternalizedPropertyPrefix(value = "myprefix", delimiter = "/")
  static interface PrefixWithDelimiterProxyInterface {
    @ExternalizedProperty("test/property")
    String property();

    @ResolverFacade
    String resolve(String propertyName);

    String noAnnotation();
  }
}
