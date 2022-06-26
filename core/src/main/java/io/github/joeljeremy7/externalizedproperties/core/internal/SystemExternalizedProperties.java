package io.github.joeljeremy7.externalizedproperties.core.internal;

import io.github.joeljeremy7.externalizedproperties.core.Converter;
import io.github.joeljeremy7.externalizedproperties.core.ConverterFacade;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperties;
import io.github.joeljeremy7.externalizedproperties.core.ExternalizedProperty;
import io.github.joeljeremy7.externalizedproperties.core.Resolver;
import io.github.joeljeremy7.externalizedproperties.core.ResolverFacade;
import io.github.joeljeremy7.externalizedproperties.core.TypeReference;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpander;
import io.github.joeljeremy7.externalizedproperties.core.VariableExpanderFacade;
import io.github.joeljeremy7.externalizedproperties.core.internal.proxy.InvocationHandlerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static io.github.joeljeremy7.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The built-in {@link ExternalizedProperties} implementation.
 */
public class SystemExternalizedProperties implements ExternalizedProperties {
    private static final Set<Class<?>> SUPPORTED_TARGET_TYPES = supportedTargetTypes();
    private final Resolver rootResolver;
    private final Converter<?> rootConverter;
    private final VariableExpander variableExpander;
    private final InvocationHandlerFactory invocationHandlerFactory;
    private final InvocationContextFactory invocationContextFactory;

    /**
     * Constructor.
     * 
     * @param rootResolver The root resolver.
     * @param rootConverter The root converter.
     * @param variableExpander The variable expander.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public SystemExternalizedProperties(
            Resolver rootResolver,
            Converter<?> rootConverter,
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
        this.invocationContextFactory = new InvocationContextFactory(this);
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
                invocationContextFactory
            )
        );
    }

    private static <T> void validate(Class<T> proxyInterface) {
        for (Method proxyMethod : proxyInterface.getMethods()) {
            throwIfVoidReturnType(proxyMethod);
            throwIfExclusiveAnnotationsAreFound(proxyMethod);
            throwIfInvalidResolverFacadeMethodSignature(proxyMethod);
            throwIfInvalidConverterFacadeMethodSignature(proxyMethod);
            throwIfInvalidVariableExpanderFacadeMethodSignature(proxyMethod);
        }
    }

    private static void throwIfVoidReturnType(Method proxyMethod) {
        Class<?> returnType = proxyMethod.getReturnType();
        if (returnType.equals(Void.TYPE) ||  returnType.equals(Void.class)) {
            throw new IllegalArgumentException(
                "Proxy methods must not have void return type."
            );
        }
    }

    private static void throwIfExclusiveAnnotationsAreFound(Method proxyMethod) {
        Annotation[] d = proxyMethod.getAnnotations();
        String[] annotationNames = Arrays.stream(d)
            .map(Annotation::annotationType)
            .filter(SystemExternalizedProperties::isExclusiveAnnotation)
            .map(Class::getName)
            .toArray(String[]::new);

        if (annotationNames.length > 1) {
            throw new IllegalArgumentException(
                "Exclusive annotations detected: " + Arrays.toString(annotationNames) +
                ". These annotations may only be use exclusive of each other."
            );
        }
    }

    private static void throwIfInvalidResolverFacadeMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(ResolverFacade.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length == 0 || parameterTypes.length > 2) {
            throw new IllegalArgumentException(String.format(
                "Resolver facades must either accept a single 'property name' parameter (%s) " +
                "or two parameters - the 'property name' (%s) and the 'target type' (%s).",
                String.class.getName(),
                String.class.getName(),
                SUPPORTED_TARGET_TYPES
            ));
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(String.format(
                "Resolver facades must accept 'property name' (%s) as first parameter.",
                String.class.getName()
            ));
        }

        if (parameterTypes.length == 2) {
            Class<?> secondParameterType = parameterTypes[1];
            if (!SUPPORTED_TARGET_TYPES.contains(secondParameterType)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Resolver facades must accept 'target type' (%s)" + 
                        "as second parameter.",
                        SUPPORTED_TARGET_TYPES
                    )
                );
            }
        }
    }

    private static void throwIfInvalidConverterFacadeMethodSignature(Method proxyMethod) {
        if (!proxyMethod.isAnnotationPresent(ConverterFacade.class)) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length == 0 || parameterTypes.length > 2) {
            throw new IllegalArgumentException(String.format(
                "Converter facades must either accept a single 'value to convert' parameter (%s) " +
                "or two parameters - the 'value to convert' (%s) and the 'target type' (%s).",
                String.class.getName(),
                String.class.getName(),
                SUPPORTED_TARGET_TYPES
            ));
        }


        Class<?> firstParamType = parameterTypes[0];
        if (!String.class.equals(firstParamType)) {
            throw new IllegalArgumentException(String.format(
                "Converter facades must accept 'value to convert' (%s) as first parameter.",
                String.class.getName()
            ));
        }

        if (parameterTypes.length == 2) {
            Class<?> secondParamType = parameterTypes[1];
            if (!SUPPORTED_TARGET_TYPES.contains(secondParamType)) {
                throw new IllegalArgumentException(
                    String.format(
                        "Converter facades must accept 'target type' (%s)" + 
                        "as second parameter.",
                        SUPPORTED_TARGET_TYPES
                    )
                );
            }
        }
    }

    private static void throwIfInvalidVariableExpanderFacadeMethodSignature(Method proxyMethod) {
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

    private static Set<Class<?>> supportedTargetTypes() {
        Set<Class<?>> weakSet = Collections.newSetFromMap(new WeakHashMap<>());
        weakSet.addAll(Arrays.asList(
        TypeReference.class,
            Class.class,
            Type.class
        ));
        return Collections.unmodifiableSet(weakSet);
    }
}
