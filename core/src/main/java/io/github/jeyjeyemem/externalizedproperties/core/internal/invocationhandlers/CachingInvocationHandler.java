package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.internal.DaemonThreadFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that caches invocation results.
 */
public class CachingInvocationHandler implements InvocationHandler {

    private final InvocationHandler decorated;
    private final WeakHashMap<Method, Object> weakCache;
    private final Duration cacheItemLifetime;
    private final ScheduledExecutorService expiryScheduler = 
        Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory(CachingInvocationHandler.class.getName())
        );

    /**
     * Constructor.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param cache The cache map.
     * @param cacheItemLifetime The duration of cache items in the cache.
     */
    public CachingInvocationHandler(
            InvocationHandler decorated,
            Map<Method, Object> cache,
            Duration cacheItemLifetime
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.weakCache = new WeakHashMap<>(requireNonNull(cache, "cache"));
        this.cacheItemLifetime = requireNonNull(cacheItemLifetime, "cacheItemLifetime");
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

    private void scheduleForExpiry(Runnable expireTask) {
        expiryScheduler.schedule(
            expireTask, 
            cacheItemLifetime.toMillis(), 
            TimeUnit.MILLISECONDS
        );
    }
}
