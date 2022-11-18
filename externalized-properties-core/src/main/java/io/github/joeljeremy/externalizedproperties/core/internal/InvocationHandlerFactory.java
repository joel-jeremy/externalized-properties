package io.github.joeljeremy.externalizedproperties.core.internal;

import io.github.joeljeremy.externalizedproperties.core.Converter;
import io.github.joeljeremy.externalizedproperties.core.InvocationContext;
import io.github.joeljeremy.externalizedproperties.core.Resolver;
import io.github.joeljeremy.externalizedproperties.core.VariableExpander;
import java.lang.reflect.InvocationHandler;

/** Invocation handler factory. */
@Internal
public interface InvocationHandlerFactory {
  /**
   * Create an {@link InvocationHandler} for the specified proxy interface.
   *
   * @param proxyInterface The proxy interface.
   * @param rootResolver The root resolver.
   * @param rootConverter The root converter.
   * @param variableExpander The variable expander.
   * @param invocationContextFactory The {@link InvocationContext} factory.
   * @return The {@link InvocationHandler} instance for the specified proxy interface.
   */
  InvocationHandler create(
      Class<?> proxyInterface,
      Resolver rootResolver,
      Converter<?> rootConverter,
      VariableExpander variableExpander,
      InvocationContextFactory invocationContextFactory);
}
