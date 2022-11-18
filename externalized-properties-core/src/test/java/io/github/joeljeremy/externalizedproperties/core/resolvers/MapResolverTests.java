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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MapResolverTests {
  static final InvocationContextTestFactory<ProxyInterface> INVOCATION_CONTEXT_FACTORY =
      InvocationContextUtils.testFactory(ProxyInterface.class);

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when property source map argument is null")
    void test1() {
      assertThrows(
          IllegalArgumentException.class, () -> new MapResolver((Map<String, String>) null));
    }

    @Test
    @DisplayName("should throw when unresolved property handler argument is null")
    void test2() {
      Map<String, String> map = new HashMap<>();
      assertThrows(IllegalArgumentException.class, () -> new MapResolver(map, null));
    }

    @Test
    @DisplayName("should throw when singleton map key is null or empty")
    void singletonMapTest1() {
      assertThrows(IllegalArgumentException.class, () -> new MapResolver(null, "value"));

      assertThrows(IllegalArgumentException.class, () -> new MapResolver("", "value"));
    }

    @Test
    @DisplayName("should throw when singleton map value is null")
    void singletonMapTest2() {
      assertThrows(IllegalArgumentException.class, () -> new MapResolver("key", null));
    }
  }

  @Nested
  class ResolveMethod {
    @Test
    @DisplayName("should resolve values from the given map")
    void test1() {
      Map<String, String> map = new HashMap<>();
      map.put("property", "property.value");

      MapResolver resolver = resolverToTest(map);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "property");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(map.get("property"), result.get());
    }

    @Test
    @DisplayName("should return empty Optional " + "when property is not found from the given map")
    void test2() {
      MapResolver resolver = resolverToTest(Collections.emptyMap());
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "property");

      assertNotNull(result);
      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName(
        "should invoke unresolved property handler "
            + "when property is not found from the given map")
    void test3() {
      AtomicBoolean unresolvedPropertyHandlerInvoked = new AtomicBoolean(false);

      UnresolvedPropertyHandler unresolvedPropertyHandler =
          propertyName -> {
            unresolvedPropertyHandlerInvoked.set(true);
            return propertyName + "-default-value";
          };

      MapResolver resolver = resolverToTest(Collections.emptyMap(), unresolvedPropertyHandler);
      InvocationContext context =
          INVOCATION_CONTEXT_FACTORY.fromMethodReference(
              ProxyInterface::property, externalizedProperties(resolver));

      Optional<String> result = resolver.resolve(context, "property");

      assertNotNull(result);
      assertTrue(result.isPresent());
      assertEquals(unresolvedPropertyHandler.handle("property"), result.get());
    }
  }

  static MapResolver resolverToTest(Map<String, String> map) {
    return new MapResolver(map);
  }

  static MapResolver resolverToTest(
      Map<String, String> map, UnresolvedPropertyHandler unresolverPropertyHandler) {
    return new MapResolver(map, unresolverPropertyHandler);
  }

  static ExternalizedProperties externalizedProperties(Resolver... resolvers) {
    return ExternalizedProperties.builder().resolvers(resolvers).build();
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();
  }
}
