package io.github.jeyjeyemem.externalizedproperties.core.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;

import java.lang.reflect.InvocationHandler;

/**
 * Invocation handler factory.
 */
public interface InvocationHandlerFactory {
    /**
     * Create an {@link InvocationHandler} for the specified proxy interface.
     * 
     * @param externalizedProperties The externalized properties.
     * @param proxyInterface The proxy interface.
     * @return The {@link InvocationHandler} for the specified proxy interface.
     */
    InvocationHandler createInvocationHandler(
        ExternalizedProperties externalizedProperties,
        Class<?> proxyInterface
    );

    /**
     * Compose an {@link InvocationHandler} based from this factory.
     * 
     * @param composeFunction The compose function.
     * @return A new invocation handler factory that creates an invocation handler 
     * instance based from this invocation handler factory.
     */
    default InvocationHandlerFactory compose(ComposeFunction composeFunction) {
        return (externalizedProperties, proxyInterface) -> 
            composeFunction.compose(
                this, 
                externalizedProperties, 
                proxyInterface
            );
    }

    /**
     * Invocation handler factory compose function.
     */
    public static interface ComposeFunction {
        /**
         * Create an {@link InvocationHandler} based from the result of another 
         * invocation handler factory. 
         * 
         * @param before The invocation handler factory to build from.
         * @param externalizedProperties The externalized properties.
         * @param proxyInterface The proxy interface.
         * @return The {@link InvocationHandler} for the specified proxy interface.
         */
        InvocationHandler compose(
            InvocationHandlerFactory before, 
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface
        );
    }
}