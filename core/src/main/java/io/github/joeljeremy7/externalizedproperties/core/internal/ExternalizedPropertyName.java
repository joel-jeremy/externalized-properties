package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
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
     *  <li>It will be derived from {@link ExternalizedProperty#value()}, if specified.</li> 
     *  <li>
     *  Otherwise, if {@link ExternalizedProperty#value()} is not specified, the property name 
     *  will be derived from the proxy method's arguments. Specifically, the first argument of 
     *  the proxy method (the method must only have one String argument e.g. 
     *  {@code String resolve(String propertyName)}).
     *  </li>
     * </ol>
     * 
     * @param proxyMethod The proxy method.
     * @param args The proxy method invocation arguments.
     * @return The externalized property name derived from {@link ExternalizedProperty#value()}, 
     * or from proxy method arguments if {@link ExternalizedProperty#value()}
     * is not specified. Otherwise, an empty {@link Optional}.
     */
    public static Optional<String> fromProxyMethodInvocation(
            ProxyMethod proxyMethod,
            Object[] args
    ) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.findAnnotation(ExternalizedProperty.class)
                .orElse(null);
        
        if (externalizedProperty == null) {
            return Optional.empty();
        }
        
        return determineExternalizedPropertyName(externalizedProperty, args);
    }

    /**
     * Determine the externalized property name from the proxy method invocation.
     * 
     * <ol>
     *  <li>It will be derived from {@link ExternalizedProperty#value()}, if specified.</li> 
     *  <li>
     *  Otherwise, if {@link ExternalizedProperty#value()} is not specified, the property name 
     *  will be derived from the proxy method's arguments. Specifically, the first argument of 
     *  the proxy method (the method must only have one String argument e.g. 
     *  {@code String resolve(String propertyName)}).
     *  </li>
     * </ol>
     * 
     * @param proxyMethod The proxy method.
     * @param invocationArgs The proxy method invocation arguments.
     * @return The externalized property name derived from {@link ExternalizedProperty#value()}, 
     * or from proxy method arguments if {@link ExternalizedProperty#value()}
     * is not specified. Otherwise, an empty {@link Optional}.
     */
    public static Optional<String> fromProxyMethodInvocation(
            Method proxyMethod, 
            Object[] invocationArgs
    ) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        
        if (externalizedProperty == null) {
            return Optional.empty();
        }
        
        return determineExternalizedPropertyName(externalizedProperty, invocationArgs);
    }

    private static Optional<String> determineExternalizedPropertyName(
            ExternalizedProperty externalizedProperty,
            Object[] invocationArgs
    ) {
        String value = externalizedProperty.value();
        if (!"".equals(value)) {
            return Optional.of(value);
        }

        // No value specified in the @ExternalizedProperty annotation.
        // Check method invocation arguments for the externalized property name.
        String nameFromArguments = null;
        if (invocationArgs.length > 0 && invocationArgs[0] instanceof String) {
            nameFromArguments = (String)invocationArgs[0];
        }

        if (nameFromArguments == null || nameFromArguments.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                "No @%s value was specified. Please provide the " + 
                "externalized property name via method arguments. " +
                "Only String values are allowed. Null or empty values are not allowed.",
                ExternalizedProperty.class.getName()
            ));
        }

        return Optional.of(nameFromArguments);
    }
}
