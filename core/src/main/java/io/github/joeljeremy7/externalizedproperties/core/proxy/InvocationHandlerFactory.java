package io.github.joeljeremy7.externalizedproperties.core.proxy;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;

import java.lang.reflect.InvocationHandler;

/**
 * Invocation handler factory.
 */
public interface InvocationHandlerFactory<T extends InvocationHandler> {
    /**
     * Create an {@link InvocationHandler} for the specified proxy interface.
     * 
     * @param rootResolver The root resolver.
     * @param rootConverter The root converter.
     * @param proxyInterface The proxy interface.
     * @return The {@link InvocationHandler} instance for the specified proxy interface.
     */
    T create(
        Resolver rootResolver,
        Converter<?> rootConverter,
        Class<?> proxyInterface
    );
}