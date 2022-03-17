package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertiesBuilder;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.internal.cachestrategies.WeakHashMapCacheStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that eagerly loads property methods
 * that are annotated with {@link ExternalizedProperty} annotation.
 */
public class EagerLoadingInvocationHandler implements InvocationHandler {

    private final InvocationHandler decorated;
    private final CacheStrategy<Method, Object> cacheStrategy;

    /**
     * Constructor.
     * 
     * @apiNote It is recommended that the {@link CacheStrategy} implementation only holds
     * weak references to the {@link Method} key in order to avoid leaks and class 
     * unloading issues. 
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link Method} and whose values
     * are the eagerly resolved properties. It is recommended that the {@link CacheStrategy} 
     * implementation only holds weak references to the {@link Method} key in order to avoid
     * leaks and class unloading issues.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    private EagerLoadingInvocationHandler(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }

    /**
     * Factory method.
     * 
     * @apiNote It is recommended that the {@link CacheStrategy} implementation only holds
     * weak references to the {@link Method} key in order to avoid leaks and class 
     * unloading issues.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param resolver The externalized properties resolver.
     * @param converter The externalized properties converter.
     * @param processor The externalized properties processor.
     * @param proxyInterface The proxy interface whose methods are annotated with
     * {@link ExternalizedProperty} annotations.
     * @param cacheStrategy The cache strategy keyed by a {@link Method} and whose values
     * are the eagerly resolved properties. It is recommended that the {@link CacheStrategy} 
     * implementation only holds weak references to the {@link Method} key in order to avoid
     * leaks and class unloading issues.
     * @return An {@code EagerLoadingInvocationHandler} whose properties have been eagerly loaded.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public static EagerLoadingInvocationHandler eagerLoad(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy,
            Resolver resolver,
            Converter<?> converter,
            Processor processor,
            Class<?> proxyInterface
    ) {
        eagerLoadProperties(
            requireNonNull(cacheStrategy, "cacheStrategy"),
            requireNonNull(resolver, "resolver"),
            requireNonNull(converter, "converter"),
            requireNonNull(processor, "processor"),
            requireNonNull(proxyInterface, "proxyInterface")
        );

        return new EagerLoadingInvocationHandler(
            decorated, 
            cacheStrategy
        );
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Optional<Object> cached = cacheStrategy.get(method);
        if (cached.isPresent()) {
            return cached.get();
        }

        Object result = decorated.invoke(proxy, method, args);
        if (result != null) {
            cacheStrategy.cache(method, result);
        }

        return result;
    }

    private static void eagerLoadProperties(
            CacheStrategy<Method, Object> cacheStrategy,
            Resolver resolver,
            Converter<?> converter,
            Processor processor,
            Class<?> proxyInterface
    ) {
        List<Method> supportedMethods = getSupportedMethods(proxyInterface);
        if (supportedMethods.isEmpty()) {
            return;
        }
        
        // Single use instance to load properties.
        // This is non-eager loading to avoid recursion.
        ExternalizedProperties nonEagerLoading = 
            ExternalizedPropertiesBuilder.newBuilder()
                .resolvers(resolver)
                .converters(converter)
                .processors(processor)
                .build();
        
        Object proxy = nonEagerLoading.proxy(proxyInterface);
        
        for (Method method : supportedMethods) {
            try {
                // Will throw if property cannot be resolved.
                Object result = method.invoke(proxy);
                cacheStrategy.cache(method, result);
            } 
            catch (Exception ex) {
                // Fail fast.
                throw new ExternalizedPropertiesException(
                    "Error occurred while eager loading properties.",
                    ex
                );
            }
        }
    }

    private static List<Method> getSupportedMethods(Class<?> proxyInterface) {
        Method[] methods = proxyInterface.getMethods();
        List<Method> supportedMethods = new ArrayList<>(methods.length);
        for (Method candidate : methods) {
            if (candidate.getParameterCount() > 0) {
                // Methods with parameters are not supported;
                continue;
            }

            // Everything after here are methods with no parameters.

            // Methods with annotation.
            if (candidate.isAnnotationPresent(ExternalizedProperty.class)) {
                supportedMethods.add(candidate);
                continue;
            }

            // No annotation but is default interface method.
            if (candidate.isDefault()) {
                supportedMethods.add(candidate);
                continue;
            }
        }
        return supportedMethods;
    }
}
