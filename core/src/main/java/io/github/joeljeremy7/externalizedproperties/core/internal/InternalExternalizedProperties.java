package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy7.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.ProxyMethodFactory;
import io.github.joeljeremy7.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.joeljeremy7.externalizedproperties.core.proxy.InvocationHandlerFactory;

import java.lang.annotation.Annotation;
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
            throwIfExclusiveAnnotationsAreFound(proxyMethod);
            throwIfInvalidResolverFacadeMethodSignature(proxyMethod);
            throwIfInvalidConverterFacadeSignature(proxyMethod);
            throwIfInvalidVariableExpanderFacadeMethodSignature(proxyMethod);
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

    private void throwIfExclusiveAnnotationsAreFound(Method proxyMethod) {
        Annotation[] d = proxyMethod.getAnnotations();
        String[] annotationNames = Arrays.stream(d)
            .map(Annotation::annotationType)
            .filter(InternalExternalizedProperties::isExclusiveAnnotation)
            .map(Class::getName)
            .toArray(String[]::new);

        if (annotationNames.length > 1) {
            throw new IllegalArgumentException(
                "Exclusive annotations detected: " + Arrays.toString(annotationNames) +
                ". These annotations may only be use exclusive of each other."
            );
        }
    }

    private void throwIfInvalidResolverFacadeMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(ResolverFacade.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                "Resolver facades must have a single parameter."
            );
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(
                "Resolver facades must have a single String parameter."
            );
        }
    }

    private void throwIfInvalidConverterFacadeSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(ConverterFacade.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 2) {
            throw new IllegalArgumentException(
                "Converter facades must have 2 parameters."
            );
        }

        Class<?> firstParamType = parameterTypes[0];
        if (!String.class.equals(firstParamType)) {
            throw new IllegalArgumentException(
                "Converter facades must have a String as first parameter."
            );
        }

        Class<?> secondParamType = parameterTypes[1];
        if (!TypeReference.class.equals(secondParamType) &&
                !Class.class.equals(secondParamType) &&
                !Type.class.equals(secondParamType)) {
            throw new IllegalArgumentException(
                String.format(
                    "Converter facades must have any one of the following " + 
                    "as second parameter: %s",
                    Arrays.asList(
                        TypeReference.class.getName(),
                        Class.class.getName(),
                        Type.class.getName()
                    )
                )
            );
        }
    }

    private void throwIfInvalidVariableExpanderFacadeMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(VariableExpanderFacade.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                "Variable expander facades must have a single parameter."
            );
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(
                "Variable expander facades must have a single String parameter."
            );
        }

        if (!String.class.equals(proxyMethod.getReturnType())) {
            throw new IllegalArgumentException(
                "Variable expander facades must have a String return type."
            );
        }
    }

    private static boolean isExclusiveAnnotation(Class<?> annotationType) {
        // Add annotations here that must be exclusive of one another.
        if (ExternalizedProperty.class.equals(annotationType) ||
                ResolverFacade.class.equals(annotationType) ||
                ConverterFacade.class.equals(annotationType) ||
                VariableExpanderFacade.class.equals(annotationType)
        ) {
            return true;
        }
        return false;
    }
}
