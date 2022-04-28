package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperty;
import io.github.jeyjeyemem.externalizedproperties.core.ResolverProvider;
import io.github.jeyjeyemem.externalizedproperties.core.internal.conversion.RootConverter;
import io.github.jeyjeyemem.externalizedproperties.core.internal.resolvers.RootResolver;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link ExternalizedProperties} implementation.
 */
public class InternalExternalizedProperties implements ExternalizedProperties {

    private final ResolverProvider<RootResolver> rootResolverProvider;
    private final RootConverter.Provider rootConverterProvider;
    private final InvocationHandlerFactory<?> invocationHandlerFactory;

    /**
     * Constructor.
     * 
     * @param rootResolverProvider The root resolver provider.
     * @param rootConverterProvider The root converter provider.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public InternalExternalizedProperties(
            ResolverProvider<RootResolver> rootResolverProvider,
            RootConverter.Provider rootConverterProvider,
            InvocationHandlerFactory<?> invocationHandlerFactory
    ) {
        requireNonNull(rootResolverProvider, "rootResolverProvider");
        requireNonNull(rootConverterProvider, "rootConverterProvider");
        requireNonNull(invocationHandlerFactory, "invocationHandlerFactory");
        this.rootResolverProvider = ResolverProvider.memoize(rootResolverProvider);
        this.rootConverterProvider = RootConverter.Provider.memoize(rootConverterProvider);
        this.invocationHandlerFactory = invocationHandlerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T proxy(Class<T> proxyInterface) {
        requireNonNull(proxyInterface, "proxyInterface");
        return proxy(proxyInterface, proxyInterface.getClassLoader());
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> proxyInterface, ClassLoader classLoader) {
        requireNonNull(proxyInterface, "proxyInterface");
        requireNonNull(classLoader, "classLoader");

        // Validate everything at init time so we can safely skip checks
        // later at resolve time for performance.
        validate(proxyInterface);
        
        return (T)Proxy.newProxyInstance(
            classLoader, 
            new Class<?>[] { proxyInterface },
            invocationHandlerFactory.create(
                rootResolverProvider.get(this), 
                rootConverterProvider.get(this), 
                proxyInterface
            )
        );
    }

    private <T> void validate(Class<T> proxyInterface) {
        for (Method proxyMethod : proxyInterface.getMethods()) {
            throwIfVoidReturnType(proxyMethod);
            throwIfInvalidMethodSignature(proxyMethod);
        }
    }

    private <T> void throwIfVoidReturnType(Method proxyMethod) {
        if (proxyMethod.getReturnType().equals(Void.TYPE) || 
            proxyMethod.getReturnType().equals(Void.class)
        ) {
            throw new IllegalArgumentException(
                "Proxy methods must not have void return type."
            );
        }
    }

    private <T> void throwIfInvalidMethodSignature(Method proxyMethod) {
        ExternalizedProperty externalizedProperty = 
            proxyMethod.getAnnotation(ExternalizedProperty.class);
        // No need to validate method signature if method is not annotated with 
        // @ExternalizedProperty or ExternalizedProperty.value() is specified.
        if (externalizedProperty == null || !"".equals(externalizedProperty.value())) {
            return;
        }

        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                "Proxy method must have a single parameter."
            );
        }

        Class<?> parameterType = parameterTypes[0];
        if (!String.class.equals(parameterType)) {
            throw new IllegalArgumentException(
                "Proxy method must have a single String parameter."
            );
        }
    }
}
