package io.github.jeyjeyemem.externalizedproperties.core;

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
     * Compose an {@link InvocationHandler} based from the result of this factory.
     * 
     * @param compose The compose function.
     * @return The composed invocation handler factory.
     */
    default InvocationHandlerFactory compose(ComposeFunction compose) {
        return (externalizedProperties, proxyInterface) -> 
            compose.compose(
                this::createInvocationHandler, 
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
         * @param toDecorate The invocation handler factory to build from.
         * @param externalizedProperties The externalized properties.
         * @param proxyInterface The proxy interface.
         * @return The {@link InvocationHandler} for the specified proxy interface.
         */
        InvocationHandler compose(
            InvocationHandlerFactory toDecorate, 
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface
        );
    }
}