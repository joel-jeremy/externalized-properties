package io.github.jeyjeyemem.externalizedproperties.core.internal;

import io.github.jeyjeyemem.externalizedproperties.core.Converter;
import io.github.jeyjeyemem.externalizedproperties.core.ExternalizedProperties;
import io.github.jeyjeyemem.externalizedproperties.core.Processor;
import io.github.jeyjeyemem.externalizedproperties.core.Resolver;
import io.github.jeyjeyemem.externalizedproperties.core.VariableExpander;
import io.github.jeyjeyemem.externalizedproperties.core.proxy.InvocationHandlerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.jeyjeyemem.externalizedproperties.core.internal.Arguments.requireNonNull;

/**
 * The default {@link ExternalizedProperties} implementation.
 */
public class InternalExternalizedProperties implements ExternalizedProperties {

    private final Resolver resolver;
    private final Processor processor;
    private final Converter<?> converter;
    private final VariableExpander variableExpander;
    private final InvocationHandlerFactory invocationHandlerFactory;

    /**
     * Constructor.
     * 
     * @param resolver The resolver.
     * @param processor The processor.
     * @param converter The converter.
     * @param variableExpander The variable expander.
     * @param invocationHandlerFactory The invocation handler factory.
     */
    public InternalExternalizedProperties(
            Resolver resolver,
            Processor processor,
            Converter<?> converter,
            VariableExpander variableExpander,
            InvocationHandlerFactory invocationHandlerFactory
    ) {
        this.resolver = requireNonNull(resolver, "resolver");
        this.converter = requireNonNull(converter, "converter");
        this.processor = requireNonNull(processor, "processor");
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
    public Resolver resolver() {
        return resolver;
    }

    /** {@inheritDoc} */
    @Override
    public Processor processor() {
        return processor;
    }

    /** {@inheritDoc} */
    @Override
    public Converter<?> converter() {
        return converter;
    }

    /** {@inheritDoc} */
    @Override
    public VariableExpander variableExpander() {
        return variableExpander;
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
