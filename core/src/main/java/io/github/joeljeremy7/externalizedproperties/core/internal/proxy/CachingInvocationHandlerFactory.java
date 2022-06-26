package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationCacheKey;
import io.github.joeljeremy7.externalizedproperties.core.internal.InvocationContextFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.caching.WeakHashMapCacheStrategy;

import java.lang.reflect.Method;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The factory for {@link CachingInvocationHandler}.
 */
public class CachingInvocationHandlerFactory 
    implements InvocationHandlerFactory {

    private final InvocationHandlerFactory decorated;
    private final CacheStrategy<InvocationCacheKey, Object> cacheStrategy;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link InvocationHandlerFactory} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link InvocationCacheKey} and whose values
     * are the resolved properties. It is recommended that the {@link CacheStrategy} 
     * implementation only holds weak references to the {@link InvocationCacheKey} due to it holding a 
     * reference to the invoked {@link Method}. This is in order to avoid leaks and class 
     * unloading issues.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public CachingInvocationHandlerFactory(
            InvocationHandlerFactory decorated,
            CacheStrategy<InvocationCacheKey, Object> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /** {@inheritDoc} */
    @Override
    public CachingInvocationHandler create(
            Class<?> proxyInterface,
            Resolver rootResolver,
            Converter<?> rootConverter,
            VariableExpander variableExpander,
            InvocationContextFactory invocationContextFactory
    ) { 
        return new CachingInvocationHandler(
            decorated.create(
                proxyInterface,
                rootResolver,
                rootConverter,
                variableExpander,
                invocationContextFactory
            ),
            cacheStrategy
        );
    }
}
