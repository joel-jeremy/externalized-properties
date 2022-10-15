package io.github.joeljeremy.externalizedproperties.core;

/** The context object for proxy method invocations. */
public interface InvocationContext {
  /**
   * The {@link ExternalizedProperties} instance that initialized the proxy which declares the
   * invoked method.
   *
   * @return The {@link ExternalizedProperties} instance that initialized the proxy which declares
   *     the invoked method.
   */
  ExternalizedProperties externalizedProperties();

  /**
   * A readonly view to the invoked proxy method.
   *
   * @return A readonly view to the invoked proxy method.
   */
  ProxyMethod method();

  /**
   * An immutable copy of the proxy method invocation arguments.
   *
   * @return An immutable copy of the proxy method invocation arguments.
   */
  InvocationArguments arguments();
}
