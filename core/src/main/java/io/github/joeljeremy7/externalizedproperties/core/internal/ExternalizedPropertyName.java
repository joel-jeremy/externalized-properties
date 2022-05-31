package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.proxy.ProxyMethod;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Utility class to determine the externalized property name from proxy methods.
 */
public class ExternalizedPropertyName {
    private ExternalizedPropertyName() {}

    /**
     * Determine the externalized property name from the proxy method invocation.
     * 
     * <ol>
     *  <li>
     *  If method is annotated with {@link ExternalizedProperty}, the externalized property 
     *  name will be derived from {@link ExternalizedProperty#value()}.
     * </li> 
     *  <li>
     *  If method is annotated with {@link ResolverFacade}, the externalized property name 
     *  will be derived from the proxy method's invocation arguments.
     *  </li>
     * </ol>
     * 
     * @see ExternalizedProperty
     * @see ResolverFacade
     * 
     * @param proxyMethod The proxy method.
     * @param invocationArgs The proxy method invocation arguments.
     * @return The externalized property name derived from {@link ExternalizedProperty#value()}, 
     * or from proxy method arguments if method is annotated with {@link ResolverFacade}. 
     * Otherwise, an empty {@link Optional}.
     */
    public static Optional<String> fromProxyMethodInvocation(
            ProxyMethod proxyMethod,
            Object[] invocationArgs
    ) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.findAnnotation(ExternalizedProperty.class)
                .orElse(null);
        if (externalizedProperty != null) {
            return Optional.of(externalizedProperty.value());
        }

        ResolverFacade resolverFacade = 
            proxyMethod.findAnnotation(ResolverFacade.class).orElse(null);
        if (resolverFacade != null) {
            return Optional.of(determineNameFromInvocationArgs(invocationArgs));
        }
        
        return Optional.empty();
    }

    /**
     * Determine the externalized property name from the proxy method invocation.
     * 
     * <ol>
     *  <li>
     *  If method is annotated with {@link ExternalizedProperty}, the externalized property 
     *  name will be derived from {@link ExternalizedProperty#value()}.
     * </li> 
     *  <li>
     *  If method is annotated with {@link ResolverFacade}, the externalized property name 
     *  will be derived from the proxy method's invocation arguments.
     *  </li>
     * </ol>
     * 
     * @see ExternalizedProperty
     * @see ResolverFacade
     * 
     * @param proxyMethod The proxy method.
     * @param invocationArgs The proxy method invocation arguments.
     * @return The externalized property name derived from {@link ExternalizedProperty#value()}, 
     * or from proxy method arguments if method is annotated with {@link ResolverFacade}. 
     * Otherwise, an empty {@link Optional}.
     */
    public static Optional<String> fromProxyMethodInvocation(
            Method proxyMethod, 
            Object[] invocationArgs
    ) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        if (externalizedProperty != null) {
            return Optional.of(externalizedProperty.value());
        }

        ResolverFacade resolverFacade = 
            proxyMethod.getAnnotation(ResolverFacade.class);
        if (resolverFacade != null) {
            return Optional.of(determineNameFromInvocationArgs(invocationArgs));
        }

        return Optional.empty();
    }

    private static String determineNameFromInvocationArgs(Object[] invocationArgs) {
        // No value specified in the @ExternalizedProperty annotation.
        // Check method invocation arguments for the externalized property name.
        String nameFromArguments = null;
        if (invocationArgs.length > 0 && invocationArgs[0] instanceof String) {
            nameFromArguments = (String)invocationArgs[0];
        }

        if (nameFromArguments == null || nameFromArguments.isEmpty()) {
            throw new IllegalArgumentException(
                "Please provide the externalized property name via method arguments. " +
                "Only String values are allowed. Null or empty values are not allowed."
            );
        }

        return nameFromArguments;
    }
}
