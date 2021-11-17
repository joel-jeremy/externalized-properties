package io.github.jeyjeyemem.externalizedproperties.core.internal.invocationhandlers;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.internal.ExternalizedPropertyMethod;
import io.github.jeyjeyemem.externalizedproperties.core.internal.MethodHandleFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties.
 * This handles invocations of methods that are marked with {@link ExternalizedProperty} annotation.
 */
public class ExternalizedPropertyInvocationHandler implements InvocationHandler {
    private final ExternalizedProperties externalizedProperties;
    private final MethodHandleFactory methodHandleFactory = new MethodHandleFactory();
    // Take care not holding a strong reference to the Method instance inside
    // the ExternalizedPropertyMethod.
    private final Map<Method, ExternalizedPropertyMethod> weakMethodCache = 
        new WeakHashMap<>();
    // 3 native object methods handling: equals, hashCode, toString
    private final Map<Method, ObjectMethod> nativeObjectMethods =
        new WeakHashMap<>(3);

    /**
     * Constructor.
     * 
     * @param externalizedProperties The externalized properties.
     */
    public ExternalizedPropertyInvocationHandler(
            ExternalizedProperties externalizedProperties
    ) {
        this.externalizedProperties = requireNonNull(
            externalizedProperties,
            "externalizedProperties"
        );

        // Native object method functions.
        try {
            nativeObjectMethods.put(
                Object.class.getDeclaredMethod("equals", Object.class), 
                ExternalizedPropertyInvocationHandler::proxyEquals
            );
            nativeObjectMethods.put(
                Object.class.getDeclaredMethod("toString"), 
                ExternalizedPropertyInvocationHandler::proxyToString
            );
            nativeObjectMethods.put(
                Object.class.getDeclaredMethod("hashCode"), 
                ExternalizedPropertyInvocationHandler::proxyHashCode
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load Object methods.", ex);
        }
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
        // Handle invocations to native Object methods:
        // toString, equals, hashCode
        ObjectMethod objectMethod = nativeObjectMethods.get(method);
        if (objectMethod != null) {
            return objectMethod.invoke(proxy, args);
        }

        ExternalizedPropertyMethod propertyMethod = weakMethodCache.get(method);
        if (propertyMethod == null) {
            propertyMethod = ExternalizedPropertyMethod.create(
                proxy, 
                method,
                externalizedProperties,
                methodHandleFactory
            );
            weakMethodCache.putIfAbsent(method, propertyMethod);
        }

        return propertyMethod.resolveProperty(args);
    }

    private static int proxyHashCode(Object proxy, Object[] args) {
        return System.identityHashCode(proxy);
    }

    // Only do reference equality.
    private static boolean proxyEquals(Object proxy, Object[] args) {
        return proxy == args[0];
    }

    private static String proxyToString(Object proxy, Object[] args) {
        return proxy.getClass().getName() + '@' + 
            Integer.toHexString(proxyHashCode(proxy, args));
    }

    private static interface ObjectMethod {
        Object invoke(Object proxy, Object[] args);
    }
}