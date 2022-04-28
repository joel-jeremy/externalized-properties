package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakHashMapCacheStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that caches invocation results.
 */
public class CachingInvocationHandler implements InvocationHandler {

    private final InvocationHandler decorated;
    private final CacheStrategy<Method, Object> cacheStrategy;
    private final Map<Method, Boolean> cacheExclusion;

    /**
     * Constructor.
     * 
     * @apiNote It is recommended that the {@link CacheStrategy} implementation only holds
     * weak references to the {@link Method} key in order to avoid leaks and class 
     * unloading issues.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link Method} and whose values
     * are the resolved properties. It is recommended that the {@link CacheStrategy} 
     * implementation only holds weak references to the {@link Method} key in order to avoid
     * leaks and class unloading issues.
     * @param proxyInterface The proxy interface.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public CachingInvocationHandler(
            InvocationHandler decorated,
            CacheStrategy<Method, Object> cacheStrategy,
            Class<?> proxyInterface
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
        this.cacheExclusion = buildCacheExclusion(
            requireNonNull(proxyInterface, "proxyInterface")
        );
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return cacheStrategy.get(method).orElseGet(() -> getAndCache(proxy, method, args));
    }

    private Object getAndCache(Object proxy, Method method, Object[] args) {
        try {
            Object result = decorated.invoke(proxy, method, args);
            if (result != null && !cacheExclusion.containsKey(method)) {
                cacheStrategy.cache(method, result);
            }
            return result;
        } catch (Throwable e) {
            throw new ExternalizedPropertiesException(
                "Error occurred while invoking decorated invocation handler.",
                e
            );
        }
    }

    private Map<Method, Boolean> buildCacheExclusion(Class<?> proxyInterface) {
        // Exclude methods annotated with @ExternalizedProperty but have no value().
        // For these methods, the property name is derived from method arguments. 
        Method[] proxyMethods = proxyInterface.getMethods();
        // Must be a WeakHashMap to prevent holding a reference to the method.
        Map<Method, Boolean> exclusion = new WeakHashMap<>(proxyMethods.length);
        for (Method proxyMethod : proxyMethods) {
            ExternalizedProperty externalizedProperty = 
                proxyMethod.getAnnotation(ExternalizedProperty.class);
            if (externalizedProperty != null && "".equals(externalizedProperty.value())) {
                exclusion.put(proxyMethod, true);
            }
        }
        return exclusion;
    }
}
