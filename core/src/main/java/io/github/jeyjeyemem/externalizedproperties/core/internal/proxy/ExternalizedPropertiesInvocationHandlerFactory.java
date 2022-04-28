package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;

/**
 * The factory for {@link ExternalizedPropertiesInvocationHandler}.
 */
public class ExternalizedPropertiesInvocationHandlerFactory 
        implements InvocationHandlerFactory<ExternalizedPropertiesInvocationHandler> {

    /** {@inheritDoc} */
    @Override
    public ExternalizedPropertiesInvocationHandler create(
            Resolver resolver, 
            Converter<?> converter,
            Class<?> proxyInterface
    ) {
        return new ExternalizedPropertiesInvocationHandler(
            resolver,
            converter
        );
    }
    
}
