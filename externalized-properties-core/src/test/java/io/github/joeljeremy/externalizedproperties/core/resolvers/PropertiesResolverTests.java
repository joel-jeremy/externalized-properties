package io.github.joeljeremy.externalizedproperties.core.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.resolvers.MapResolver.UnresolvedPropertyHandler;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.InvocationContextUtils.InvocationContextTestFactory;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PropertiesResolverTests {
  private static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);
  private static final Properties EMPTY_PROPERTIES = new Properties();

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when properties argument is null")
    void test1() {
      assertThrows(IllegalArgumentException.class, () -> new PropertiesResolver((Properties) null));
    }

    @Test
    @DisplayName("should throw when unresolved property handler argument is null")
    void test2() {
      assertThrows(
          IllegalArgumentException.class, () -> new PropertiesResolver(EMPTY_PROPERTIES, null));
    }

    @Test
    @DisplayName("should ignore properties with non-String keys or values")
    void test3() {
      Properties props = new Properties();
      props.put("property.nonstring", 123);
      props.put(123, "property.nonstring.key");
      props.put("property", "property.value");

      PropertiesResolver resolver = resolverToTest(props);
      InvocationContext intPropertyContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::propertyNonString, externalizedProperties(resolver));
      InvocationContext propertyContext =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> nonStringResult = resolver.resolve(intPropertyContext, "property.nonstring");
      Optional<String> result = resolver.resolve(propertyContext, "property");

      assertFalse(nonStringResult.isPresent());
      assertTrue(result.isPresent());

      assertEquals(props.get("property"), result.get());
    }
  }

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve property value from the given properties")
    void test1() {
      Properties props = new Properties();
      props.setProperty("property", "property.value");

      PropertiesResolver resolver = resolverToTest(props);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "property");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(props.getProperty("property"), result.get());
    }

    @Test
    @DisplayName(
        "should return empty Optional " + "when property is not found from the given properties")
    void test2() {
      PropertiesResolver resolver = resolverToTest(EMPTY_PROPERTIES);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result =
          resolver.resolve(
              context, "property" // Not in Properties.
              );

      assertNotNull(result);
      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
        "should invoke unresolved property handler "
            + "when property is not found from the given properties")
    void test3() {
      AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

      UnresolvedPropertyHandler unresolvedPropertyHandler =
          propertyName -> {
            unresolvedPropertyHandlerInvoked.set(true);
            return propertyName + "-default-value";
          };

      PropertiesResolver resolver = resolverToTest(EMPTY_PROPERTIES, unresolvedPropertyHandler);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "property");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(unresolvedPropertyHandler.handle("property"), result.get());
    }
  }

  private static PropertiesResolver resolverToTest(Properties properties) {
    return new PropertiesResolver(properties);
  }

  private static PropertiesResolver resolverToTest(
      Properties properties, UnresolvedPropertyHandler unresolverPropertyHandler) {
    return new PropertiesResolver(properties, unresolverPropertyHandler);
  }

  private static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  private static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();

    @ExternalizedProperty("property.nonstring")
    int propertyNonString();
  }
}
