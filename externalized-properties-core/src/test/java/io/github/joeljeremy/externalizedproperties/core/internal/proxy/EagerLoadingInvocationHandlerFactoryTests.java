package io.github.joeljeremy.externalizedproperties.core.internal.proxy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy.externalizedproperties.core.conversion.converters.DefaultConverter;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationCacheKey;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationContextFactory;
import io.github.joeljeremy.externalizedproperties.core.internal.InvocationHandlerFactory;
import io.github.joeljeremy.externalizedproperties.core.internal.caching.WeakHashMapCacheStrategy;
import io.github.joeljeremy.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy.externalizedproperties.core.internal.processing.RootProcessor;
import io.github.joeljeremy.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy.externalizedproperties.core.testfixtures.StubResolver;
import io.github.joeljeremy.externalizedproperties.core.variableexpansion.SimpleVariableExpander;
import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EagerLoadingInvocationHandlerFactoryTests {
  static final Resolver RESOLVER = new StubResolver();
  static final Converter<?> CONVERTER = new DefaultConverter();
  static final VariableExpander VARIABLE_EXPANDER = new SimpleVariableExpander();
  static final ExternalizedProperties EXTERNALIZED_PROPERTIES =
      ExternalizedProperties.builder().resolvers(RESOLVER).converters(CONVERTER).build();

  static final Resolver ROOT_RESOLVER =
      new RootResolver(Arrays.asList(RESOLVER), new RootProcessor());
  static final Converter<?> ROOT_CONVERTER = new RootConverter(CONVERTER);

  static final InvocationContextFactory INVOCATION_CONTEXT_FACTORY =
      new InvocationContextFactory(EXTERNALIZED_PROPERTIES);

  static final InvocationHandlerFactory BASE_INVOCATION_HANDLER_FACTORY =
      new ExternalizedPropertiesInvocationHandlerFactory();

  @Nested
  class Constructor {
    @Test
    @DisplayName("should throw when decorated argument is null")
    void test1() {
      WeakHashMapCacheStrategy<InvocationCacheKey, Object> cacheStrategy =
          new WeakHashMapCacheStrategy<>();
      assertThrows(
          IllegalArgumentException.class,
          () -> new EagerLoadingInvocationHandlerFactory(null, cacheStrategy));
    }

    @Test
    @DisplayName("should throw when cache strategy argument is null")
    void test2() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new EagerLoadingInvocationHandlerFactory(BASE_INVOCATION_HANDLER_FACTORY, null));
    }
  }

  @Nested
  class CreateMethod {
    @Test
    @DisplayName("should not return null")
    void test1() {
      InvocationHandlerFactory factory =
          new EagerLoadingInvocationHandlerFactory(
              BASE_INVOCATION_HANDLER_FACTORY, new WeakHashMapCacheStrategy<>());

      assertNotNull(
          factory.create(
              ProxyInterface.class,
              ROOT_RESOLVER,
              ROOT_CONVERTER,
              VARIABLE_EXPANDER,
              INVOCATION_CONTEXT_FACTORY));
    }

    @Test
    @DisplayName("should return instance of EagerLoadingInvocationHandler")
    void test2() {
      InvocationHandlerFactory factory =
          new EagerLoadingInvocationHandlerFactory(
              BASE_INVOCATION_HANDLER_FACTORY, new WeakHashMapCacheStrategy<>());

      InvocationHandler result =
          factory.create(
              ProxyInterface.class,
              ROOT_RESOLVER,
              ROOT_CONVERTER,
              VARIABLE_EXPANDER,
              INVOCATION_CONTEXT_FACTORY);

      assertTrue(result instanceof EagerLoadingInvocationHandler);
    }
  }

  static interface ProxyInterface {
    @ExternalizedProperty("property")
    String property();
  }
}
