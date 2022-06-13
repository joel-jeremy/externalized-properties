package io.github.joeljeremy7.externalizedproperties.core.internal;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * The method invocation cache key.
 */
public final class InvocationCacheKey {
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
     * @param args The method invocation args. May be {@code null} since 
     * proxy instances provides {@code null} args array to invocation 
     * handlers instead of an empty array if method proxy method has no 
     * parameters.
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