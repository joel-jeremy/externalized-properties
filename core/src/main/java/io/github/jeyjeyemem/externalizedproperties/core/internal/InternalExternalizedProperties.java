package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedPropertyResolver;
import io.github.jeyjeyemem.externalizedproperties.core.InvocationHandlerFactory;
import io.github.jeyjeyemem.externalizedproperties.core.TypeReference;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.conversion.Converter;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.utils.Arguments.requireNonNull;

/**
 * The default {@link ExternalizedProperties} implementation.
 */
public class InternalExternalizedProperties implements ExternalizedProperties {

    private final ExternalizedPropertyResolver externalizedPropertyResolver;
    private final Converter converter;
    private final VariableExpander variableExpander;
    private final InvocationHandlerFactory invocationHandlerFactory;

    /**
     * Constructor.
     * 
     * @param externalizedPropertyResolver The externalized property resolver.
     * @param converter The converter.
     * @param variableExpander The variable expander.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public InternalExternalizedProperties(
            ExternalizedPropertyResolver externalizedPropertyResolver,
            Converter converter,
            VariableExpander variableExpander,
            InvocationHandlerFactory invocationHandlerFactory
    ) {
        this.externalizedPropertyResolver = requireNonNull(
            externalizedPropertyResolver, 
            "externalizedPropertyResolver"
        );
        this.converter = requireNonNull(
            converter, 
            "converter"
        );
        this.variableExpander = requireNonNull(variableExpander, "variableExpander");
        this.invocationHandlerFactory = requireNonNull(
            invocationHandlerFactory, 
            "invocationHandlerFactory"
        );
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

        validate(proxyInterface);
        
        return (T) Proxy.newProxyInstance(
            classLoader, 
            new Class<?>[] { proxyInterface },
            invocationHandlerFactory.createInvocationHandler(this, proxyInterface)
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> resolveProperty(String propertyName) {
        return externalizedPropertyResolver.resolve(propertyName);
    }
    
    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            Class<T> expectedType
    ) {
        return (Optional<T>)resolveProperty(propertyName, (Type)expectedType);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolveProperty(
            String propertyName,
            TypeReference<T> expectedType
    ) {
        return (Optional<T>)resolveProperty(
            propertyName, 
            requireNonNull(expectedType, "expectedType").type()
        );
    }

    /** {@inheritDoc} */
    @Override
    public Optional<?> resolveProperty(
            String propertyName,
            Type expectedType
    ) {
        Optional<String> resolved = resolveProperty(propertyName);

        if (String.class.equals(expectedType)) {
            return resolved;
        }

        return resolved.map(r -> {
            return converter.convert(
                resolved.get(), 
                expectedType
            );
        });
    }

    /** {@inheritDoc} */
    @Override
    public String expandVariables(String source) {
        return variableExpander.expandVariables(source);
    }

    private <T> void validate(Class<T> proxyInterface) {
        requireNoVoidReturningMethods(proxyInterface);
    }

    private <T> void requireNoVoidReturningMethods(Class<T> proxyInterface) {
        List<String> voidReturningMethods = Arrays.stream(proxyInterface.getMethods())
            .filter(m -> m.getReturnType().equals(Void.TYPE))
            .map(Method::toGenericString)
            .collect(Collectors.toList());

        if (!voidReturningMethods.isEmpty()) {
            throw new IllegalArgumentException(
                "Proxy interface methods must not return void. " +
                "Invalid Methods: " + voidReturningMethods
            );
        }
    }
}
