package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.CacheStrategy;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

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
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param externalizedProperties The externalized properties instance.
     * @param proxyInterface The proxy interface whose methods are annotated with
     * {@link ExternalizedProperty} annotations.
     * @param cacheStrategy The cache strategy keyed by a {@link Method} and whose values
     * are the eagerly resolved properties. This cache strategy should weakly hold on to the 
     * {@link Method} key in order to avoid leaks and class unloading issues. 
     */
    public EagerLoadingInvocationHandler(
            InvocationHandler decorated,
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface,
            CacheStrategy<Method, Object> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");

        eagerLoadProperties(
            requireNonNull(externalizedProperties, "externalizedProperties"),
            cacheStrategy,
            requireNonNull(proxyInterface, "proxyInterface")
        );
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Optional<Object> cached = cacheStrategy.getFromCache(method);
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
            ExternalizedProperties externalizedProperties,
            CacheStrategy<Method, Object> cacheStrategy,
            Class<?> proxyInterface
    ) {
        for (Method method : proxyInterface.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(ExternalizedProperty.class)) {
                continue;
            }

            ExternalizedProperty externalizedProperty = method.getAnnotation(
                ExternalizedProperty.class
            );
            String propertyName = externalizedProperties.expandVariables(
                externalizedProperty.value()
            );
            Optional<Object> result = externalizedProperties.resolveProperty(
                propertyName,
                method.getGenericReturnType()
            );

            if (result.isPresent()) {
                cacheStrategy.cache(method, result.get());
            }
        }
    }
}
