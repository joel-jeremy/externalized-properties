package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.Convert;
import io.github.joeljeremy7.externalizedproperties.core.ExpandVariables;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ProxyMethodFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.InvocationHandlerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link ExternalizedProperties} implementation.
 */
public class InternalExternalizedProperties implements ExternalizedProperties {
    private final RootResolver rootResolver;
    private final RootConverter rootConverter;
    private final VariableExpander variableExpander;
    private final InvocationHandlerFactory invocationHandlerFactory;
    private final ProxyMethodFactory proxyMethodFactory;

    /**
     * Constructor.
     * 
     * @param rootResolver The root resolver.
     * @param rootConverter The root converter.
     * @param variableExpander The variable expander.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public InternalExternalizedProperties(
            RootResolver rootResolver,
            RootConverter rootConverter,
            VariableExpander variableExpander,
            InvocationHandlerFactory invocationHandlerFactory
    ) {
        requireNonNull(rootResolver, "rootResolver");
        requireNonNull(rootConverter, "rootConverter");
        requireNonNull(variableExpander, "variableExpander");
        requireNonNull(invocationHandlerFactory, "invocationHandlerFactory");
        this.rootResolver = rootResolver;
        this.rootConverter = rootConverter;
        this.variableExpander = variableExpander;
        this.invocationHandlerFactory = invocationHandlerFactory;
        this.proxyMethodFactory = new ProxyMethodFactory(this);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T initialize(Class<T> proxyInterface) {
        requireNonNull(proxyInterface, "proxyInterface");
        return initialize(proxyInterface, proxyInterface.getClassLoader());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T initialize(Class<T> proxyInterface, ClassLoader classLoader) {
        requireNonNull(proxyInterface, "proxyInterface");
        requireNonNull(classLoader, "classLoader");

        // Try to validate everything at init time so we can safely skip checks
        // later at resolve time for performance.
        validate(proxyInterface);
        
        return (T)Proxy.newProxyInstance(
            classLoader, 
            new Class<?>[] { proxyInterface },
            invocationHandlerFactory.create(
                proxyInterface,
                rootResolver,
                rootConverter,
                variableExpander,
                proxyMethodFactory
            )
        );
    }

    private <T> void validate(Class<T> proxyInterface) {
        for (Method proxyMethod : proxyInterface.getMethods()) {
            throwIfVoidReturnType(proxyMethod);
            throwIfInvalidMethodSignature(proxyMethod);
            throwIfInvalidConvertMethodSignature(proxyMethod);
            throwIfInvalidExpandVariablesMethodSignature(proxyMethod);
        }
    }

    private void throwIfVoidReturnType(Method proxyMethod) {
        if (proxyMethod.getReturnType().equals(Void.TYPE) || 
            proxyMethod.getReturnType().equals(Void.class)
        ) {
            throw new IllegalArgumentException(
                "Proxy methods must not have void return type."
            );
        }
    }

    private void throwIfInvalidMethodSignature(Method proxyMethod) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        // No need to validate method signature if method is not annotated with 
        // @ExternalizedProperty or ExternalizedProperty.value() is specified.
        if (externalizedProperty == null || !"".equals(externalizedProperty.value())) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(String.format(
                "Proxy methods annotated with @%s must have a single parameter.",
                ExternalizedProperty.class
            ));
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(String.format(
                "Proxy methods annotated with @%s must have a single String parameter.",
                ExternalizedProperty.class
            ));
        }
    }

    private void throwIfInvalidConvertMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(Convert.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 2) {
            throw new IllegalArgumentException(
                String.format(
                    "Proxy methods annotated with @%s must have 2 parameters.", 
                    Convert.class.getSimpleName()
                )
            );
        }

        Class<?> firstParamType = parameterTypes[0];
        if (!String.class.equals(firstParamType)) {
            throw new IllegalArgumentException(
                String.format(
                    "Proxy methods annotated with @%s must have " + 
                    "%s as first parameter.", 
                    Convert.class.getSimpleName(),
                    String.class.getName()
                )
            );
        }

        Class<?> secondParamType = parameterTypes[1];
        if (!TypeReference.class.equals(secondParamType) &&
                !Class.class.equals(secondParamType) &&
                !Type.class.equals(secondParamType)) {
            throw new IllegalArgumentException(
                String.format(
                    "Proxy methods annotated with @%s must have one of the following " + 
                    "as second parameter: %s", 
                    Convert.class.getSimpleName(),
                    Arrays.asList(
                        TypeReference.class.getName(),
                        Class.class.getName(),
                        Type.class.getName()
                    )
                )
            );
        }
    }

    private void throwIfInvalidExpandVariablesMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(ExpandVariables.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(String.format(
                "Proxy methods annotated with @%s must have a single parameter.",
                ExpandVariables.class.getName()
            ));
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(String.format(
                "Proxy methods annotated with @%s must have a single String parameter.",
                ExpandVariables.class.getName()
            ));
        }

        if (!String.class.equals(proxyMethod.getReturnType())) {
            throw new IllegalArgumentException(String.format(
                "Proxy methods annotated with @%s must have a String return type.",
                ExpandVariables.class.getName()
            ));
        }
    }
}
