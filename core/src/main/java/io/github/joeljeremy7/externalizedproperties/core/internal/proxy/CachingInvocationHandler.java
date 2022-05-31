package io.github.joeljeremy7.externalizedproperties.core.internal.proxy;

import io.github.joeljeremy7.externalizedproperties.core.CacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedPropertiesException;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakConcurrentHashMapCacheStrategy;
import io.github.joeljeremy7.externalizedproperties.core.internal.cachestrategies.WeakHashMapCacheStrategy;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Implementation of {@link InvocationHandler} that caches invocation results.
 */
public class CachingInvocationHandler implements InvocationHandler {

    private final InvocationHandler decorated;
    private final CacheStrategy<InvocationCacheKey, Object> cacheStrategy;

    /**
     * Constructor.
     * 
     * @apiNote It is recommended that the {@link CacheStrategy} implementation only holds
     * weak references to the {@link Method} key in order to avoid leaks and class 
     * unloading issues.
     * 
     * @param decorated The decorated {@link InvocationHandler} instance.
     * @param cacheStrategy The cache strategy keyed by a {@link InvocationCacheKey} and whose values
     * are the resolved properties. It is recommended that the {@link CacheStrategy} 
     * implementation only holds weak references to the {@link InvocationCacheKey} due to it holding a 
     * reference to the invoked {@link Method}. This is in order to avoid leaks and class 
     * unloading issues.
     * 
     * @see WeakConcurrentHashMapCacheStrategy
     * @see WeakHashMapCacheStrategy
     */
    public CachingInvocationHandler(
            InvocationHandler decorated,
            CacheStrategy<InvocationCacheKey, Object> cacheStrategy
    ) {
        this.decorated = requireNonNull(decorated, "decorated");
        this.cacheStrategy = requireNonNull(cacheStrategy, "cacheStrategy");
    }
    
    /** {@inheritDoc} */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationCacheKey cacheKey = new InvocationCacheKey(method, args);
        Optional<?> cached = cacheStrategy.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        return getAndCache(proxy, cacheKey);
    }

    private Object getAndCache(Object proxy, InvocationCacheKey cacheKey) {
        try {
            Object result = decorated.invoke(proxy, cacheKey.method, cacheKey.args);
            if (result != null) {
                cacheStrategy.cache(cacheKey, result);
            }
            return result;
        } catch (Throwable e) {
            throw new ExternalizedPropertiesException(
                "Error occurred while invoking decorated invocation handler.",
                e
            );
        }
    }

    /**
     * The method invocation cache key.
     */
    public final static class InvocationCacheKey {
        private final Method method;
        private final @Nullable Object[] args;
        private final int hashCode;

        /**
         * Constructor.
         * 
         * @param method The invoked method.
         */
        public InvocationCacheKey(Method method) {
            // null because proxies give null instead of 
            // empty array when there are no invocation args.
            this(method, null);
        }

        /**
         * Constructor.
         * 
         * @param method The invoked method.
         * @param args The method invocation args.
         */
        public InvocationCacheKey(Method method, @Nullable Object[] args) {
            this.method = method;
            this.args = args;
            this.hashCode = recursiveHashCode(method, args);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return hashCode;
        }

        /**
         * Two {@link InvocationCacheKey}s with the same combinations of {@code Method}
         * and {@code Object[]} arguments are considered equal.
         * 
         * @param obj The other object to compare with.
         * @return {@code true} if both {@link InvocationCacheKey}s have the same combination 
         * of {@code Method} and {@code Object[]} arguments. Otherwise, {@code false}.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof InvocationCacheKey) {
                InvocationCacheKey other = (InvocationCacheKey)obj;
                return Objects.equals(method, other.method) &&
                    Arrays.deepEquals(args, other.args);
            }
            return false;
        }

        private static int recursiveHashCode(@Nullable Object... items) {
            return Arrays.deepHashCode(items);
        }
    }
}
