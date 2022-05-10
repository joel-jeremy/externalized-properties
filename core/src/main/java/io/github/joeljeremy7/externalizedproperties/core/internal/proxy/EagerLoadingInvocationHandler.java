package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakHashMapCacheStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that eagerly loads property methods
 * that are annotated with {@link ExternalizedProperty} annotation.
 */
public class EagerLoadingInvocationHandler extends CachingInvocationHandler {

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
     * @param proxyInterface The proxy interface.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    private EagerLoadingInvocationHandler(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy,
            Class<?> proxyInterface
    ) {
        super(decorated, cacheStrategy, proxyInterface);
    }

    /**
     * Factory method.
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
     * @param proxyInterface The proxy interface.
     * @return An {@code EagerLoadingInvocationHandler} whose properties have been eagerly loaded.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public static EagerLoadingInvocationHandler eagerLoad(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy,
            Class<?> proxyInterface
    ) {
        loadPropertiesToCache(
            requireNonNull(decorated, "decorated"),
            requireNonNull(cacheStrategy, "cacheStrategy"),
            requireNonNull(proxyInterface, "proxyInterface")
        );

        return new EagerLoadingInvocationHandler(
            decorated, 
            cacheStrategy,
            proxyInterface
        );
    }

    private static void loadPropertiesToCache(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy,
            Class<?> proxyInterface
    ) {
        List<Method> supportedMethods = getSupportedMethods(proxyInterface);
        if (supportedMethods.isEmpty()) {
            return;
        }

        // Create a single-use proxy for eager loading.
        Object proxy = Proxy.newProxyInstance(
            proxyInterface.getClassLoader(), 
            new Class<?>[] { proxyInterface }, 
            decorated
        );
        
        for (Method method : supportedMethods) {
            try {
                Object result = method.invoke(proxy);
                if (result != null) {
                    cacheStrategy.cache(method, result);
                }
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
                // Methods with parameters are not supported.
                continue;
            }

            // Everything after here are methods with no parameters.

            // Methods with annotation.
            if (candidate.isAnnotationPresent(ExternalizedProperty.class)) {
                supportedMethods.add(candidate);
            }
            // No annotation but is default interface method.
            else if (candidate.isDefault()) {
                supportedMethods.add(candidate);
            }
        }
        return supportedMethods;
    }
}

