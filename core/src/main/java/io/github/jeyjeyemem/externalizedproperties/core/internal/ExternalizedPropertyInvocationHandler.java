package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.StringVariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.annotations.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.ResolvedPropertyConverter;
import io.github.jeyjeyemem.externalizedproperties.core.exceptions.ExternalizedPropertiesException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * Invocation handler for Externalized Properties.
 * This handles invocations of methods that are marked with {@link ExternalizedProperty}.
 */
public class ExternalizedPropertyInvocationHandler implements InvocationHandler {
    private static final String HASH_CODE_METHOD_NAME = "hashCode";
    private static final String EQUALS_METHOD_NAME = "equals";
    private static final String TO_STRING_METHOD_NAME = "toString";

    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final StringVariableExpander variableExpander;
    private final ResolvedPropertyConverter resolvedPropertyConverter;
    private final MethodHandleFactory methodHandleFactory = new MethodHandleFactory();

    /**
     * Constructor.
     * 
     * @param externalizedPropertyResolver The externalized property resolver.
     * @param resolvedPropertyConverter The resolved property converter.
     * @param variableExpander The string variable expander.
     */
    public ExternalizedPropertyInvocationHandler(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            ResolvedPropertyConverter resolvedPropertyConverter,
            StringVariableExpander variableExpander
    ) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.resolvedPropertyConverter = requireNonNull(
            resolvedPropertyConverter, 
            "resolvedPropertyConverter"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
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
        if (isNativeObjectMethod(method)) {
            return handleNativeObjectMethod(
                proxy, 
                method, 
                args
            );
        }

        ExternalizedPropertyMethod propertyMethodProxy = 
            new ExternalizedPropertyMethod(
                proxy, 
                method,
                externalizedPropertyResolver,
                resolvedPropertyConverter,
                variableExpander,
                methodHandleFactory
            );

        return propertyMethodProxy.resolveProperty(args);
    }

    private Object handleNativeObjectMethod(
            Object proxy,
            Method method, 
            Object[] args
    ) {
        switch(method.getName()) {
            case TO_STRING_METHOD_NAME:
                return proxyToString(proxy);
            case EQUALS_METHOD_NAME:
                return proxyEquals(proxy, args[0]);
            case HASH_CODE_METHOD_NAME:
                return proxyHashCode(proxy);
            default:
                throw new ExternalizedPropertiesException(
                    "Method is not a native Object method."
                );
        }
    }

    private int proxyHashCode(Object proxy) {
        return System.identityHashCode(proxy);
    }

    // Only do reference equality.
    private boolean proxyEquals(Object proxy, Object other) {
        return proxy == other;
    }

    private String proxyToString(Object proxy) {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxyHashCode(proxy));
    }

    private boolean isNativeObjectMethod(Method method) {
        return TO_STRING_METHOD_NAME.equals(method.getName()) ||
            EQUALS_METHOD_NAME.equals(method.getName()) ||
            HASH_CODE_METHOD_NAME.equals(method.getName());
    }
}