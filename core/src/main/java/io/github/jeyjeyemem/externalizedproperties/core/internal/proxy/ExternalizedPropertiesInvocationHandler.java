package io.github.jeyjeyemem.externalizedproperties.core.internal.proxy;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties.
 * This handles invocations of methods that are marked with {@link ExternalizedProperty} annotation.
 */
public class ExternalizedPropertiesInvocationHandler implements InvocationHandler {
    private final ProxyMethodHandler proxyMethodHandler;

    /**
     * Constructor.
     * 
     * @param resolver The resolver.
     * @param converter The converter.
     */
    public ExternalizedPropertiesInvocationHandler(
            Resolver resolver,
            Converter<?> converter
    ) {
        this.proxyMethodHandler = new ProxyMethodHandler(
            requireNonNull(resolver, "resolver"),
            requireNonNull(converter, "converter"),
            new MethodHandleFactory()
        );
    }

    /**
     * Handles the externalized properties proxy method invocation.
     * 
     * @param proxy The proxy object.
     * @param method The invoked method.
     * @param args The method invocation arguments.
     * @return Method return value.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Handle invocations to native Object methods:
        // toString, equals, hashCode
        Object objectMethodResult = handleIfObjectMethod(proxy, method, args);
        if (objectMethodResult != null) {
            return objectMethodResult;
        }

        return proxyMethodHandler.handle(proxy, method, args != null ? args : new Object[0]);
    }
    
    // Avoid calling methods in proxy object to avoid recursion.
    private static @Nullable Object handleIfObjectMethod(
            Object proxy, 
            Method method, 
            Object[] args
    ) {
        if ("toString".equals(method.getName())) {
            return proxyToString(proxy);
        }
        else if ("equals".equals(method.getName()) && method.getParameterTypes().length == 1) {
            return proxyEquals(proxy, args);
        }
        else if ("hashCode".equals(method.getName())) {
            return proxyHashCode(proxy);
        }

        return null;
    }

    private static int proxyHashCode(Object proxy) {
        return System.identityHashCode(proxy);
    }

    // Only do reference equality.
    private static boolean proxyEquals(Object proxy, Object[] args) {
        return proxy == args[0];
    }

    private static String proxyToString(Object proxy) {
        return proxy.getClass().getName() + '@' + 
            Integer.toHexString(proxyHashCode(proxy));
    }
}