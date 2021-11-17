package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.DaemonThreadFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that eagerly loads property methods
 * that are annotated with {@link ExternalizedProperty} annotation.
 */
public class EagerLoadingInvocationHandler implements InvocationHandler {

    private final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(EagerLoadingInvocationHandler.class.getName())
        );
    private final InvocationHandler decorated;
    private final WeakHashMap<Method, Object> weakCache;
    private final Duration cacheItemLifetime;

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param externalizedProperties The externalized properties instance.
     * @param proxyInterface The proxy interface whose methods are annotated with
     * {@link ExternalizedProperty} annotations.
     * @param cache The cache map.
     * @param cacheItemLifetime The duration of cache items in the cache.
     */
    public EagerLoadingInvocationHandler(
            InvocationHandler decorated,
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface,
            Map<Method, Object> cache,
            Duration cacheItemLifetime
    ) {
        requireNonNull(decorated, "decorated");
        requireNonNull(externalizedProperties, "externalizedProperties");
        requireNonNull(proxyInterface, "proxyInterface");

        this.decorated = requireNonNull(decorated, "decorated");
        this.weakCache = new WeakHashMap<>(requireNonNull(cache, "cache"));
        this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");

        eagerLoadProperties(externalizedProperties, proxyInterface);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object cached = weakCache.get(method);
        if (cached != null) {
            return cached;
        }

        Object result = decorated.invoke(proxy, method, args);
        if (result != null) {
            weakCache.putIfAbsent(method, result);
            scheduleForExpiry(() -> weakCache.remove(method));
        }

        return result;
    }

    private void eagerLoadProperties(
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface
    ) {
        Map<Method, Object> resolvedProperties = resolveProperties(
            externalizedProperties,
            proxyInterface
        );

        weakCache.putAll(resolvedProperties);
        scheduleForExpiry(() -> weakCache.clear());
    }

    private Map<Method, Object> resolveProperties(
            ExternalizedProperties externalizedProperties,
            Class<?> proxyInterface
    ) {
        Map<Method, Object> resolvedValuesByMethod = new HashMap<>();

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
                resolvedValuesByMethod.put(method, result.get());
            }
        }

        return resolvedValuesByMethod;
    }

    private void scheduleForExpiry(Runnable expiryAction) {
        expiryScheduler.schedule(
            expiryAction, 
            cacheItemLifetime.toMillis(), 
            TimeUnit.MILLISECONDS
        );
    }
}
