package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties.
 * This handles invocations of methods that are marked with {@link ExternalizedProperty}.
 */
public class ExternalizedPropertyInvocationHandler implements InvocationHandler {
    private final ConcurrentMap<CacheKey, ExternalizedPropertyMethod> methodCache = 
        new ConcurrentHashMap<>();
    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final VariableExpander variableExpander;
    private final ResolvedPropertyConverter resolvedPropertyConverter;

    /**
     * Constructor.
     * 
     * @param externalizedPropertyResolver The externalized property resolver.
     * @param variableExpander The externalized property name variable expander.
     * @param resolvedPropertyConverter The resolved property converter.
     */
    public ExternalizedPropertyInvocationHandler(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            VariableExpander variableExpander,
            ResolvedPropertyConverter resolvedPropertyConverter
    ) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
    }

    /**
     * Handles the externalized property method invocation.
     * 
     * @param proxy The proxy object.
     * @param method The invoked method.
     * @param args The method invocation arguments.
     * @return Method return value.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ExternalizedPropertyMethod propertyMethodProxy = 
            methodCache.computeIfAbsent(
                new CacheKey(Proxy.getInvocationHandler(proxy), method), 
                m -> new ExternalizedPropertyMethod(
                    proxy, 
                    method,
                    externalizedPropertyResolver,
                    variableExpander,
                    resolvedPropertyConverter
                )
            );

        return propertyMethodProxy.resolveProperty(args);
    }

    private static class CacheKey {
        private final InvocationHandler handler;
        private final Method method;
        private final int hash;

        public CacheKey(InvocationHandler handler, Method method) {
            this.handler = handler;
            this.method = method;
            this.hash = Objects.hash(handler, method);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof CacheKey) {
                CacheKey other = (CacheKey)obj;

                return Objects.equals(handler, other.handler) &&
                    Objects.equals(method, other.method);
            }

            return false;
        }

        @Override 
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return String.format("[%s].[%s]", handler.toString(), method.getName());
        }
    }
}